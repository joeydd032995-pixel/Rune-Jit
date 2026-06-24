package net.packets

import entity.Player
import net.GamePacket
import org.slf4j.LoggerFactory

object GroundItemTakeHandler : (Player, GamePacket) -> Unit {
    private val log = LoggerFactory.getLogger(GroundItemTakeHandler::class.java)
    override fun invoke(player: Player, pkt: GamePacket) {
        val itemId = pkt.payload.readShort().toInt() and 0xFFFF
        val x      = pkt.payload.readShort().toInt() and 0xFFFF
        val y      = pkt.payload.readShort().toInt() and 0xFFFF
        log.debug("${player.username} take item $itemId at ($x,$y)")
    }
}
