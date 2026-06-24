package combat

import org.slf4j.LoggerFactory
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers combat interactions with the plugin system.
 * The NPC "Attack" handler is registered as a wildcard (all NPC IDs) until the
 * NPC database is populated by /import-osrsbox-complete.
 */
class CombatPlugin(
    private val dataDir: Path = Path.of("data/combat"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        CombatDefs.init(dataDir)

        ctx.onNpcInteract("Attack", IntArray(0)) { player, npcIndex ->
            log.info("${player.username} attacks NPC index $npcIndex")
            // Full NPC combat routing via /npc-behavior-simulator
        }

        log.info("CombatPlugin loaded — NPC attack handler registered (wildcard)")
    }

    companion object {
        private val log = LoggerFactory.getLogger(CombatPlugin::class.java)
    }
}
