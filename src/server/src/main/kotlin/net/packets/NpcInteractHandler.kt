package net.packets

import entity.Player
import net.GamePacket
import org.slf4j.LoggerFactory

object NpcInteractHandler : (Player, GamePacket) -> Unit {
    private val log = LoggerFactory.getLogger(NpcInteractHandler::class.java)
    override fun invoke(player: Player, pkt: GamePacket) {
        val npcIndex = pkt.payload.readShort().toInt() and 0xFFFF
        log.debug("${player.username} NPC interact → index $npcIndex")
        // NPC option 1 dispatch — forwarded to plugin system via PluginContext
        // Full wiring via /npc-behavior-simulator
    }
}
