package net

import io.netty.buffer.ByteBuf

/** A fully decoded incoming game packet ready for dispatch. */
data class GamePacket(val name: String, val payload: ByteBuf)
