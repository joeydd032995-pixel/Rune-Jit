package skills.firemaking

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Firemaking item interactions for all log types in firemaking.yaml.
 * All XP values, level requirements, and tick rates are loaded from the data file —
 * none are hardcoded here. Source: server-skills.md rule.
 *
 * Source: https://oldschool.runescape.wiki/w/Firemaking
 */
class FiremakingPlugin(
    private val yamlPath: Path = Path.of("data/skills/firemaking.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = FiremakingDefs.init(yamlPath)
        val logIds = defs.logs.values.map { it.itemId }.toIntArray()

        ctx.onItemInteract("Light", logIds) { player, itemId ->
            startFiremaking(player, itemId, defs)
        }
    }

    private fun startFiremaking(player: Player, itemId: Int, defs: FiremakingConfig) {
        if (!player.inventory.contains(defs.meta.tinderboxItemId)) {
            // TODO: send "You need a tinderbox to light a fire." message via packet
            return
        }

        val logDef = defs.byItemId[itemId] ?: return

        if (player.skills.getLevel(Skill.FIREMAKING) < logDef.levelRequired) {
            // TODO: send "You need level X Firemaking to light this fire." message via packet
            return
        }

        player.tickQueue.schedule(1, FiremakingAction(player, logDef, defs))
    }
}
