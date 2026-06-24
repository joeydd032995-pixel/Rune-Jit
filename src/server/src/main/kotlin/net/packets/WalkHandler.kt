package net.packets

import entity.Player
import net.GamePacket
import org.slf4j.LoggerFactory

object WalkHandler : (Player, GamePacket) -> Unit {
    private val log = LoggerFactory.getLogger(WalkHandler::class.java)
    override fun invoke(player: Player, pkt: GamePacket) {
        val destX  = pkt.payload.readShort().toInt()
        val destY  = pkt.payload.readShort().toInt()
        pkt.payload.readByte() // ctrlKey
        log.debug("${player.username} walk → ($destX, $destY)")
        // TODO: route through pathfinding engine (/pathfinding-and-clipping-engine)
    }
}
