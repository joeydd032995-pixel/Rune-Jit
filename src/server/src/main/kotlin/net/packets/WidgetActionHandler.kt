package net.packets

import entity.Player
import net.GamePacket
import org.slf4j.LoggerFactory

object WidgetActionHandler : (Player, GamePacket) -> Unit {
    private val log = LoggerFactory.getLogger(WidgetActionHandler::class.java)
    override fun invoke(player: Player, pkt: GamePacket) {
        val widgetId = pkt.payload.readInt()
        val itemId   = pkt.payload.readShort().toInt() and 0xFFFF
        val slot     = pkt.payload.readShort().toInt() and 0xFFFF
        log.debug("${player.username} widget action → widget=$widgetId item=$itemId slot=$slot")
    }
}
