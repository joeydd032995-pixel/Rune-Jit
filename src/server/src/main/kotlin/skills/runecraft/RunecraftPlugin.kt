package skills.runecraft

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Runecraft altar interactions for all altars defined in runecraft.yaml.
 * All XP values, level requirements, and item IDs are loaded from the data file —
 * none are hardcoded here. Source: server-skills.md rule.
 *
 * Clicking "Craft-rune" on any altar object triggers RunecraftAction, which consumes
 * all essence in the player's inventory and produces runes in a single tick.
 * Source: https://oldschool.runescape.wiki/w/Runecraft
 */
class RunecraftPlugin(
    private val yamlPath: Path = Path.of("data/skills/runecraft.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = RunecraftDefs.init(yamlPath)

        if (defs.allAltarObjectIds.isEmpty()) return

        ctx.onObjectInteract("Craft-rune", defs.allAltarObjectIds) { player, objectId ->
            val altar = defs.byAltarObjectId[objectId] ?: return@onObjectInteract
            startRunecraft(player, altar, defs)
        }
    }

    private fun startRunecraft(player: Player, altar: AltarDef, defs: RunecraftConfig) {
        val rcLevel = player.skills.getLevel(Skill.RUNECRAFT)
        if (rcLevel < altar.levelRequired) {
            // TODO: send "You need level X Runecraft to craft runes here." message via packet
            return
        }

        if (!player.inventory.contains(altar.essenceItemId)) {
            // TODO: send "You need essence to craft runes." message via packet
            return
        }

        val action = RunecraftAction(player, altar, defs)
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
    }
}
