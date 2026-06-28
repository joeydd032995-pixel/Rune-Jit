package skills.smithing

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Smithing interactions:
 *   - Use any smeltable ore on a furnace → smelt a bar (smelting stage).
 *   - Use any bar on an anvil → smith an item (smithing stage).
 *
 * All XP values, level requirements, bar recipes, and item bar-counts are loaded from
 * data/skills/smithing.yaml — none are hardcoded here (server-skills.md rule).
 *
 * Furnace/anvil object IDs are populated from the cache after /cache-unpack-extract-assets.
 * Until then both arrays are empty and registration is skipped (mirrors FishingPlugin),
 * so the plugin loads cleanly with no live interactions.
 *
 * Source: https://oldschool.runescape.wiki/w/Smithing
 */
class SmithingPlugin(
    private val yamlPath: Path = Path.of("data/skills/smithing.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = SmithingDefs.init(yamlPath)

        val oreIds = defs.bars.values
            .flatMap { bar -> bar.ingredients.map { it.oreItemId } }
            .distinct()
            .toIntArray()
        val barIds = defs.bars.values.map { it.barItemId }.distinct().toIntArray()
        val furnaceIds = defs.meta.furnaceObjectIds
        val anvilIds = defs.meta.anvilObjectIds

        // Smelting: use ore on furnace. Skip when either side is empty (object IDs pending cache).
        if (oreIds.isNotEmpty() && furnaceIds.isNotEmpty()) {
            ctx.onItemUseOnItem(oreIds, furnaceIds) { player, oreId, _ ->
                startSmelt(player, oreId, defs)
            }
        }

        // Smithing: use bar on anvil. Skip when either side is empty (object IDs pending cache).
        if (barIds.isNotEmpty() && anvilIds.isNotEmpty()) {
            ctx.onItemUseOnItem(barIds, anvilIds) { player, barId, _ ->
                startSmith(player, barId, defs)
            }
        }
    }

    /**
     * Begins smelting the bar matching the ore used. When an ore is shared by multiple bars
     * (e.g. coal), the first matching bar whose level the player meets and whose full recipe
     * they hold is selected.
     */
    private fun startSmelt(player: Player, oreId: Int, defs: SmithingConfig) {
        val candidates = defs.barsByOreItemId[oreId] ?: return
        val bar = candidates.firstOrNull { player.skills.getLevel(Skill.SMITHING) >= it.levelRequired }
            ?: return
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, SmithingAction.smelt(player, bar, player.tickQueue, defs))
    }

    /**
     * Begins smithing from the bar used. When a bar type can make multiple items, the
     * highest-level item the player can currently make is selected as a sensible default
     * (the smithing interface selection is wired once widgets exist).
     */
    private fun startSmith(player: Player, barId: Int, defs: SmithingConfig) {
        val barDef = defs.barByItemId[barId] ?: return
        if (!player.inventory.contains(defs.meta.hammerItemId)) {
            // TODO: send "You need a hammer to work the metal with." message via packet
            return
        }
        val smithLevel = player.skills.getLevel(Skill.SMITHING)
        val item = defs.smithByBarType[barDef.name]
            ?.filter { smithLevel >= it.levelRequired }
            ?.maxByOrNull { it.levelRequired }
            ?: return
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, SmithingAction.smith(player, item, player.tickQueue, defs))
    }
}
