package net

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

/**
 * Prepends the ISAAC-encrypted opcode byte to each outgoing ByteBuf.
 * The caller is responsible for building the payload ByteBuf; this encoder
 * reads the first byte as the raw opcode, encrypts it, then appends the rest.
 *
 * Convention: callers write `rawOpcode:byte` followed by `payload...` to the channel.
 * Source: OSRS server-to-client packet format
 */
class GameEncoder(private val encodeCipher: IsaacCipher) : MessageToByteEncoder<ByteBuf>() {
    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        if (msg.readableBytes() < 1) return
        val rawOpcode = msg.readByte().toInt() and 0xFF
        val encryptedOpcode = (rawOpcode + encodeCipher.nextInt()) and 0xFF
        out.writeByte(encryptedOpcode)
        out.writeBytes(msg)
    }
}
