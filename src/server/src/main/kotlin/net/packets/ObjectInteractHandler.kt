package net.packets

import entity.Player
import net.GamePacket
import org.slf4j.LoggerFactory

object ObjectInteractHandler : (Player, GamePacket) -> Unit {
    private val log = LoggerFactory.getLogger(ObjectInteractHandler::class.java)
    override fun invoke(player: Player, pkt: GamePacket) {
        val objectId = pkt.payload.readShort().toInt() and 0xFFFF
        val x        = pkt.payload.readShort().toInt() and 0xFFFF
        val y        = pkt.payload.readShort().toInt() and 0xFFFF
        log.debug("${player.username} object interact → id=$objectId ($x,$y)")
    }
}
