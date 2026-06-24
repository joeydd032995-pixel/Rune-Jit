package combat

import org.slf4j.LoggerFactory
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers combat interactions with the plugin system.
 * NPC "Attack" handler is pending PluginContext.onNpcInteract() support,
 * which requires the /protocol-packet-engine phase.
 *
 * TODO: register onNpcInteract("Attack", ...) once PluginContext supports NPC interactions.
 */
class CombatPlugin(
    private val dataDir: Path = Path.of("data/combat"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        CombatDefs.init(dataDir)
        log.info("CombatPlugin loaded — NPC attack registration pending PluginContext.onNpcInteract()")
    }

    companion object {
        private val log = LoggerFactory.getLogger(CombatPlugin::class.java)
    }
}
