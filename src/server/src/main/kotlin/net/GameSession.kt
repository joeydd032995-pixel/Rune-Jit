package net

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel

/** Wraps an active player connection: the Netty Channel and the ISAAC cipher pair. */
class GameSession(val channel: Channel, val isaac: IsaacRandom) {
    val isActive: Boolean get() = channel.isActive
    fun writeAndFlush(buf: ByteBuf) { channel.writeAndFlush(buf) }
    fun close() { channel.close() }
}
