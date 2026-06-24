package net

import engine.TickEngine
import entity.Player
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory
import java.nio.file.Path

private const val LOGIN_RESPONSE_OK                = 2
private const val LOGIN_RESPONSE_INVALID           = 3
private const val LOGIN_RESPONSE_REVISION_MISMATCH = 6

private const val LOGIN_TYPE_FRESH     = 16
private const val LOGIN_TYPE_RECONNECT = 18

/**
 * OSRS login state machine. Handles the initial handshake on port 43594.
 *
 * Connection sequence:
 *   CLIENT to SERVER: [handshakeOpcode:1]
 *     15 = JS5 cache request, upgrade to JS5Handler
 *     14 = game login handshake, server sends server revision (4 bytes), transitions to LOGIN_TYPE
 *   CLIENT to SERVER: [loginType:1] (16=fresh, 18=reconnect)
 *   CLIENT to SERVER: [blockLen:2][rsaBlock:blockLen]
 *     RSA block (private server stub, plaintext): [magic:1=10][sessionKeys:4x4][uid:4][username:Cstring][password:Cstring]
 *   SERVER to CLIENT: [2][rights:1][0:1]  (login OK)
 *
 * Source: OSRS login protocol, https://github.com/RuneStar/cs2
 */
class LoginHandler(
    private val tickEngine: TickEngine,
    private val dispatcher: PacketDispatcher,
    private val cacheDir: Path = Path.of("cache"),
) : ChannelInboundHandlerAdapter() {

    private val log = LoggerFactory.getLogger(LoginHandler::class.java)

    private enum class State { HANDSHAKE, LOGIN_TYPE, LOGIN_BLOCK }
    private var state = State.HANDSHAKE

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is ByteBuf) return
        try {
            when (state) {
                State.HANDSHAKE   -> handleHandshake(ctx, msg)
                State.LOGIN_TYPE  -> handleLoginType(ctx, msg)
                State.LOGIN_BLOCK -> handleLoginBlock(ctx, msg)
            }
        } finally {
            msg.release()
        }
    }

    /**
     * First byte from client determines connection type:
     *   15 = JS5 cache serving (read 8 more bytes: revision+subRevision)
     *   14 = game login handshake (server replies with 0 and revision, then waits for login type)
     */
    private fun handleHandshake(ctx: ChannelHandlerContext, buf: ByteBuf) {
        if (buf.readableBytes() < 1) return
        val opcode = buf.readUnsignedByte().toInt()
        when (opcode) {
            15 -> {
                // JS5 handshake: client sends [revision:4][subRevision:4]
                if (buf.readableBytes() < 8) {
                    buf.resetReaderIndex()
                    return
                }
                val revision = buf.readInt()
                buf.readInt() // sub-revision, ignored
                val resp = ctx.alloc().buffer(1)
                if (revision != OSRS_REVISION) {
                    resp.writeByte(LOGIN_RESPONSE_REVISION_MISMATCH)
                    ctx.writeAndFlush(resp).addListener { ctx.close() }
                } else {
                    resp.writeByte(0) // proceed
                    ctx.writeAndFlush(resp)
                    ctx.pipeline().replace(this, "js5", JS5Handler(cacheDir))
                }
            }
            14 -> {
                // Game login handshake: server sends 0 + server revision (4 bytes)
                val resp = ctx.alloc().buffer(5)
                resp.writeByte(0)
                resp.writeInt(OSRS_REVISION)
                ctx.writeAndFlush(resp)
                state = State.LOGIN_TYPE
            }
            else -> {
                log.debug("Unknown handshake opcode $opcode from ${ctx.channel().remoteAddress()} -- closing")
                ctx.close()
            }
        }
    }

    private fun handleLoginType(ctx: ChannelHandlerContext, buf: ByteBuf) {
        if (buf.readableBytes() < 1) return
        val loginType = buf.readUnsignedByte().toInt()
        when (loginType) {
            LOGIN_TYPE_FRESH, LOGIN_TYPE_RECONNECT -> state = State.LOGIN_BLOCK
            else -> {
                log.warn("Unknown login type $loginType from ${ctx.channel().remoteAddress()} -- closing")
                ctx.close()
            }
        }
    }

    private fun handleLoginBlock(ctx: ChannelHandlerContext, buf: ByteBuf) {
        if (buf.readableBytes() < 2) return
        val blockLen = buf.readUnsignedShort()
        if (buf.readableBytes() < blockLen) {
            // Not enough data yet -- restore reader index and wait for more
            buf.resetReaderIndex()
            return
        }

        // Private-server stub: skip RSA decryption, read plaintext login block.
        // RSA block format: [magic:1][sessionKeys:4x4][uid:4][username:Cstring][password:Cstring]
        val magic = buf.readUnsignedByte().toInt()
        if (magic != 10) {
            log.warn("Bad login magic byte $magic -- expected 10 from ${ctx.channel().remoteAddress()}")
            sendLoginResponse(ctx, LOGIN_RESPONSE_INVALID)
            return
        }

        val sessionKeys = IntArray(4) { buf.readInt() }
        buf.readInt() // uid -- not used in private server

        val username = readNullTerminatedString(buf)
        readNullTerminatedString(buf) // password -- not validated for private server

        val isaac = IsaacRandom(sessionKeys)
        val queue = tickEngine.newQueue()
        val player = Player(username, queue)
        val session = GameSession(ctx.channel(), isaac)
        player.session = session

        log.info("Login OK: $username from ${ctx.channel().remoteAddress()}")

        // Send login response: [responseCode:1][rights:1][padding:1]
        val resp = ctx.alloc().buffer(3)
        resp.writeByte(LOGIN_RESPONSE_OK)
        resp.writeByte(0) // player rights (0=normal, 1=mod, 2=admin)
        resp.writeByte(0)
        ctx.writeAndFlush(resp)

        // Upgrade pipeline: remove login handler, add game codec
        ctx.pipeline().replace(this, "decoder", GameDecoder(isaac.decode))
        ctx.pipeline().addAfter("decoder", "encoder", GameEncoder(isaac.encode))
        ctx.pipeline().addLast("dispatcher", PacketDispatcherHandler(player, dispatcher))
    }

    private fun sendLoginResponse(ctx: ChannelHandlerContext, code: Int) {
        val resp = ctx.alloc().buffer(1)
        resp.writeByte(code)
        ctx.writeAndFlush(resp).addListener { ctx.close() }
    }

    /**
     * Reads a null-terminated (C-style) string from the buffer.
     * Stops at the first 0 byte or when the buffer is exhausted.
     */
    private fun readNullTerminatedString(buf: ByteBuf): String {
        val sb = StringBuilder()
        while (buf.readableBytes() > 0) {
            val ch = buf.readByte().toInt() and 0xFF
            if (ch == 0) break
            sb.append(ch.toChar())
        }
        return sb.toString()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.warn("Login error on ${ctx.channel().remoteAddress()}: ${cause.message}")
        ctx.close()
    }

    companion object {
        /** OSRS revision this server targets. Must match revision.yaml. */
        const val OSRS_REVISION = 238
    }
}
