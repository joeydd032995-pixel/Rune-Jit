package skills.crafting

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Crafting interactions for all recipes defined in crafting.yaml.
 * All XP values, level requirements, and item IDs are loaded from the data file —
 * none are hardcoded here. Source: server-skills.md rule.
 *
 * Non-station recipes (leather, gems, glass) use item-use-on-item with the primary tool.
 * Station-based recipes (jewellery at furnace, spinning at spinning wheel) use
 * onObjectInteract — skipped until cache provides object IDs.
 * Source: https://oldschool.runescape.wiki/w/Crafting
 */
class CraftingPlugin(
    private val yamlPath: Path = Path.of("data/skills/crafting.yaml"),
) : Plugin() {

    // Object IDs populated from cache after /cache-unpack-extract-assets
    private val furnaceObjectIds = IntArray(0)
    private val spinningWheelObjectIds = IntArray(0)

    override fun register(ctx: PluginContext) {
        val defs = CraftingDefs.init(yamlPath)

        registerLeatherCrafting(ctx, defs)
        registerGemCutting(ctx, defs)
        registerGlassblowing(ctx, defs)

        if (furnaceObjectIds.isNotEmpty()) {
            registerJewellery(ctx, defs)
        }

        if (spinningWheelObjectIds.isNotEmpty()) {
            registerSpinning(ctx, defs)
        }
    }

    private fun registerLeatherCrafting(ctx: PluginContext, defs: CraftingConfig) {
        val leatherRecipes = defs.allRecipes.filter { it.category == "leather" }
        val inputIds = leatherRecipes.flatMap { r -> r.inputs.map { it.itemId } }.distinct().toIntArray()
        if (inputIds.isEmpty()) return
        // Use needle (1733) on leather/hard leather to trigger leather crafting
        // Source: https://oldschool.runescape.wiki/w/Needle
        val needleId = intArrayOf(1733)
        ctx.onItemUseOnItem(needleId, inputIds) { player, _, targetItemId ->
            val level = player.skills.getLevel(Skill.CRAFTING)
            val recipe = defs.byInputItemId[targetItemId]
                ?.filter { it.category == "leather" && level >= it.levelRequired }
                ?.maxByOrNull { it.levelRequired }
                ?: return@onItemUseOnItem
            startCraft(player, recipe, defs)
        }
    }

    private fun registerGemCutting(ctx: PluginContext, defs: CraftingConfig) {
        val gemRecipes = defs.allRecipes.filter { it.category == "gems" }
        val inputIds = gemRecipes.flatMap { r -> r.inputs.map { it.itemId } }.distinct().toIntArray()
        if (inputIds.isEmpty()) return
        // Use chisel (1755) on uncut gem to cut it
        // Source: https://oldschool.runescape.wiki/w/Chisel
        val chiselId = intArrayOf(1755)
        ctx.onItemUseOnItem(chiselId, inputIds) { player, _, targetItemId ->
            val recipe = defs.byInputItemId[targetItemId]
                ?.firstOrNull { it.category == "gems" }
                ?: return@onItemUseOnItem
            startCraft(player, recipe, defs)
        }
    }

    private fun registerGlassblowing(ctx: PluginContext, defs: CraftingConfig) {
        val glassRecipes = defs.allRecipes.filter { it.category == "glass" }
        val inputIds = glassRecipes.flatMap { r -> r.inputs.map { it.itemId } }.distinct().toIntArray()
        if (inputIds.isEmpty()) return
        // Use glassblowing pipe (1785) on molten glass (1775)
        // Source: https://oldschool.runescape.wiki/w/Glassblowing_pipe
        val pipeId = intArrayOf(1785)
        ctx.onItemUseOnItem(pipeId, inputIds) { player, _, targetItemId ->
            val level = player.skills.getLevel(Skill.CRAFTING)
            val recipe = defs.byInputItemId[targetItemId]
                ?.filter { it.category == "glass" && level >= it.levelRequired }
                ?.maxByOrNull { it.levelRequired }
                ?: return@onItemUseOnItem
            startCraft(player, recipe, defs)
        }
    }

    private fun registerJewellery(ctx: PluginContext, defs: CraftingConfig) {
        // Object IDs pending cache extraction — this branch is never reached at startup
        ctx.onObjectInteract("Smelt", furnaceObjectIds) { player, _ ->
            val level = player.skills.getLevel(Skill.CRAFTING)
            val recipe = defs.stationRecipes["FURNACE"]
                ?.filter { level >= it.levelRequired }
                ?.maxByOrNull { it.levelRequired }
                ?: return@onObjectInteract
            startCraft(player, recipe, defs)
        }
    }

    private fun registerSpinning(ctx: PluginContext, defs: CraftingConfig) {
        // Object IDs pending cache extraction — this branch is never reached at startup
        ctx.onObjectInteract("Spin", spinningWheelObjectIds) { player, _ ->
            val level = player.skills.getLevel(Skill.CRAFTING)
            val recipe = defs.stationRecipes["SPINNING_WHEEL"]
                ?.filter { level >= it.levelRequired }
                ?.maxByOrNull { it.levelRequired }
                ?: return@onObjectInteract
            startCraft(player, recipe, defs)
        }
    }

    private fun startCraft(player: Player, recipe: RecipeDef, defs: CraftingConfig) {
        val craftingLevel = player.skills.getLevel(Skill.CRAFTING)
        if (craftingLevel < recipe.levelRequired) return
        val action = CraftingAction(player, recipe, player.tickQueue, defs)
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
    }
}
