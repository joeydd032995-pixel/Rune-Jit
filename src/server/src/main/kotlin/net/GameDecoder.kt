package net

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.slf4j.LoggerFactory

class GameDecoder(private val decodeCipher: IsaacCipher) : ByteToMessageDecoder() {
    private val log = LoggerFactory.getLogger(GameDecoder::class.java)

    // -1 = waiting for opcode, ≥0 = reading payload of this many bytes
    private var pendingSize = -1
    private var pendingName: String? = null

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        while (true) {
            if (pendingSize == -1) {
                if (buf.readableBytes() < 1) return
                val rawOpcode = buf.readUnsignedByte().toInt()
                val opcode = (rawOpcode - decodeCipher.nextInt()) and 0xFF
                pendingName = PacketRegistry.nameFor(opcode)
                pendingSize = if (pendingName != null) PacketRegistry.sizeFor(pendingName!!) else -1
                if (pendingName == null) {
                    log.debug("Unknown C2S opcode 0x${opcode.toString(16)} — skipping")
                    return
                }
            }

            val name = pendingName ?: return

            val frameSize: Int
            when (pendingSize) {
                -1 -> { // VAR_BYTE: 1-byte length prefix
                    if (buf.readableBytes() < 1) return
                    frameSize = buf.readUnsignedByte().toInt()
                }
                -2 -> { // VAR_SHORT: 2-byte length prefix
                    if (buf.readableBytes() < 2) return
                    frameSize = buf.readUnsignedShort()
                }
                else -> frameSize = pendingSize
            }

            if (buf.readableBytes() < frameSize) return
            val payload = buf.readRetainedSlice(frameSize)
            out.add(GamePacket(name, payload))
            pendingSize = -1
            pendingName = null
        }
    }
}
