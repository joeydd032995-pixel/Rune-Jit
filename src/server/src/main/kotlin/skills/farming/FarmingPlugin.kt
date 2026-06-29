package skills.farming

import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Farming patch interactions from farming.yaml.
 * Handles seed planting and harvest for all defined patches.
 * All XP values, level requirements, and item IDs are loaded from the data file.
 * Patch object IDs pending cache extraction — skips registration when objectId == 0.
 * Source: server-skills.md rule (data-driven, no hardcoded values).
 */
class FarmingPlugin(
    private val yamlPath: Path = Path.of("data/skills/farming.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = FarmingDefs.init(yamlPath)
        val activeGrowth = mutableMapOf<Int, FarmingGrowthAction>()

        for ((_, patch) in defs.patches) {
            // Skip registration when patch object ID is not yet available from cache
            if (patch.patchObjectId == 0) continue

            // Plant: use seed on patch object
            ctx.onItemUseOnItem(intArrayOf(patch.seedItemId), intArrayOf(patch.patchObjectId)) { player, _, _ ->
                val level = player.skills.getBoostedLevel(Skill.FARMING)
                if (level < patch.levelRequired) return@onItemUseOnItem
                if (!player.inventory.contains(patch.seedItemId)) return@onItemUseOnItem
                player.inventory.remove(patch.seedItemId, 1)
                val action = FarmingGrowthAction(player, patch, player.tickQueue, defs)
                activeGrowth[patch.seedItemId] = action
                player.tickQueue.schedule(patch.growthTicks, action)
            }

            // Harvest: object interact "Pick"
            ctx.onObjectInteract("Pick", intArrayOf(patch.patchObjectId)) { player, _ ->
                val action = activeGrowth[patch.seedItemId] ?: return@onObjectInteract
                if (action.isFullyGrown) {
                    action.harvest()
                    activeGrowth.remove(patch.seedItemId)
                }
            }
        }
    }
}
