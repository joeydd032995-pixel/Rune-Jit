package skills.cooking

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Cooking interactions for all food defined in cooking.yaml.
 * All XP values, level requirements, item IDs, tick rates, and burn levels are
 * loaded from the data file — none are hardcoded here. Source: server-skills.md rule.
 *
 * Two entry points (Source: https://oldschool.runescape.wiki/w/Cooking):
 *   - Using raw food on a fire   → onItemUseOnItem(rawFoodIds, fireObjectIds-as-items)
 *   - "Cook" option on a range   → onObjectInteract("Cook", rangeObjectIds)
 *
 * Fire and range object IDs come from the cache after /cache-unpack-extract-assets;
 * until then the relevant registrations are skipped (mirrors FishingPlugin's
 * empty-id guard) so no fabricated IDs leak in.
 */
class CookingPlugin(
    private val yamlPath: Path = Path.of("data/skills/cooking.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = CookingDefs.init(yamlPath)
        val rawFoodIds = defs.food.values.map { it.rawItemId }.toIntArray()

        // Raw food used on a fire object. Fire IDs are pending cache extraction.
        if (defs.fireObjectIds.isNotEmpty() && rawFoodIds.isNotEmpty()) {
            ctx.onItemUseOnItem(rawFoodIds, defs.fireObjectIds) { player, primaryId, targetId ->
                // Dispatcher normalises order, so the raw food may be either arg.
                val rawId = if (defs.byRawItemId.containsKey(primaryId)) primaryId else targetId
                startCooking(player, rawId, onRange = false, defs)
            }
        }

        // "Cook" option on a range object. Range IDs are pending cache extraction.
        if (defs.rangeObjectIds.isNotEmpty()) {
            ctx.onObjectInteract("Cook", defs.rangeObjectIds) { player, _ ->
                startBestAvailableCook(player, defs)
            }
        }
    }

    /**
     * Starts cooking a specific raw food item (fire path: the player chose the item).
     */
    private fun startCooking(player: Player, rawItemId: Int, onRange: Boolean, defs: CookingConfig) {
        val food = defs.byRawItemId[rawItemId] ?: return

        if (player.skills.getLevel(Skill.COOKING) < food.levelRequired) {
            // TODO: send "You need a Cooking level of X to cook this." message via packet
            return
        }

        val action = CookingAction(player, food, onRange, player.tickQueue, defs)
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
    }

    /**
     * Range path: the player clicked "Cook" on a range without selecting a specific
     * item. Cooks the first raw food in the inventory the player can cook. The real
     * client opens a "make-X" interface here; that selection UI is wired later — this
     * keeps the server-side action correct and data-driven in the meantime.
     */
    private fun startBestAvailableCook(player: Player, defs: CookingConfig) {
        val cookingLevel = player.skills.getLevel(Skill.COOKING)
        val food = defs.food.values
            .filter { player.inventory.contains(it.rawItemId) && cookingLevel >= it.levelRequired }
            .minByOrNull { it.levelRequired }
            ?: return

        val action = CookingAction(player, food, onRange = true, player.tickQueue, defs)
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
    }
}
