package skills.hunter

import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Hunter trap interactions for all trap types in hunter.yaml.
 * Trap placement ("Lay" on a trap item) dispatches to the highest-level creature
 * the player qualifies for with that trap type.
 * All level requirements, XP, and catch rates come from the YAML — none hardcoded here.
 */
class HunterPlugin(
    private val yamlPath: Path = Path.of("data/skills/hunter.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = HunterDefs.init(yamlPath)

        for ((trapItemId, creatures) in defs.byTrapItemId) {
            if (trapItemId == 0) continue  // pending cache extraction
            // Sort descending by level so we pick the best eligible creature first
            val sorted = creatures.sortedByDescending { it.levelRequired }
            ctx.onItemInteract("Lay", intArrayOf(trapItemId)) { player, _ ->
                val hunterLevel = player.skills.getBoostedLevel(Skill.HUNTER)
                val creature = sorted.firstOrNull { hunterLevel >= it.levelRequired } ?: return@onItemInteract
                val action = HunterAction(player, creature, player.tickQueue, defs)
                player.tickQueue.schedule(creature.checkTicks, action)
            }
        }
    }
}
