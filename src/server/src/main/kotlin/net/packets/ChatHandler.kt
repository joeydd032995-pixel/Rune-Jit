package net.packets

import entity.Player
import net.GamePacket
import org.slf4j.LoggerFactory

object ChatHandler : (Player, GamePacket) -> Unit {
    private val log = LoggerFactory.getLogger(ChatHandler::class.java)
    override fun invoke(player: Player, pkt: GamePacket) {
        val effects = pkt.payload.readByte().toInt()
        val sb = StringBuilder()
        while (pkt.payload.readableBytes() > 0) {
            val b = pkt.payload.readByte().toInt() and 0xFF
            if (b == 0) break
            sb.append(b.toChar())
        }
        log.info("${player.username} chat: $sb (effects=$effects)")
    }
}
