package net

import entity.Player
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.slf4j.LoggerFactory

class PacketDispatcher {
    private val log = LoggerFactory.getLogger(PacketDispatcher::class.java)
    private val handlers = mutableMapOf<String, (Player, GamePacket) -> Unit>()

    fun register(name: String, handler: (Player, GamePacket) -> Unit) {
        handlers[name] = handler
    }

    fun hasHandler(name: String): Boolean = name in handlers

    fun dispatch(player: Player, packet: GamePacket) {
        try {
            handlers[packet.name]?.invoke(player, packet)
                ?: log.debug("Unhandled packet: ${packet.name} for ${player.username}")
        } finally {
            packet.payload.release()
        }
    }
}

/** Netty handler that bridges inbound [GamePacket]s to [PacketDispatcher]. */
class PacketDispatcherHandler(
    private val player: Player,
    private val dispatcher: PacketDispatcher,
) : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is GamePacket) dispatcher.dispatch(player, msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        LoggerFactory.getLogger(PacketDispatcherHandler::class.java)
            .warn("Game packet error for ${player.username}: ${cause.message}")
        ctx.close()
    }
}
