package skills.herblore

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Herblore interactions: herb cleaning, unfinished-potion making, and
 * finished-potion making. All XP values, level requirements, and item IDs are loaded
 * from data/skills/herblore.yaml — none are hardcoded here (server-skills.md rule).
 *
 * All three actions are immediate (no tick loop): OSRS resolves a single herb-clean or
 * potion-mix instantly per click. Make-X batching is a client-driven repeat of the same
 * interaction and is out of scope until the Make-X interface is wired.
 *
 * Source: https://oldschool.runescape.wiki/w/Herblore
 */
class HerblorePlugin(
    private val yamlPath: Path = Path.of("data/skills/herblore.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = HerbloreDefs.init(yamlPath)

        // 1. Clean grimy herb → clean herb + XP.
        val grimyIds = defs.herbs.values.map { it.grimyItemId }.toIntArray()
        ctx.onItemInteract("Clean", grimyIds) { player, itemId ->
            cleanHerb(player, itemId, defs)
        }

        // 2. Clean herb + vial of water → unfinished potion (no XP).
        val cleanHerbIds = defs.herbs.values.map { it.cleanItemId }.toIntArray()
        ctx.onItemUseOnItem(cleanHerbIds, intArrayOf(defs.meta.vialOfWaterItemId)) { player, herbId, _ ->
            makeUnfinishedPotion(player, herbId, defs)
        }

        // 3. Secondary ingredient + unfinished potion → finished (3) dose potion + XP.
        val secondaryIds = defs.potions.values.map { it.secondaryItemId }.distinct().toIntArray()
        val unfPotionIds = defs.herbs.values.map { it.unfPotionItemId }.distinct().toIntArray()
        ctx.onItemUseOnItem(secondaryIds, unfPotionIds) { player, secondaryId, unfId ->
            makePotion(player, secondaryId, unfId, defs)
        }
    }

    private fun cleanHerb(player: Player, grimyId: Int, defs: HerbloreConfig) {
        val herb = defs.byGrimyId[grimyId] ?: return
        if (player.skills.getLevel(Skill.HERBLORE) < herb.cleanLevel) {
            // TODO: send "You need level X Herblore to clean this herb." message via packet
            return
        }
        if (!player.inventory.remove(grimyId, 1)) return
        player.inventory.addItem(herb.cleanItemId)
        player.skills.addXp(Skill.HERBLORE, herb.cleanXp)
    }

    private fun makeUnfinishedPotion(player: Player, cleanHerbId: Int, defs: HerbloreConfig) {
        val herb = defs.byCleanId[cleanHerbId] ?: return
        // No level requirement and no XP for making unfinished potions in OSRS.
        // Source: https://oldschool.runescape.wiki/w/Herblore#Unfinished_potions
        if (!player.inventory.contains(herb.cleanItemId)) return
        if (!player.inventory.contains(defs.meta.vialOfWaterItemId)) return
        player.inventory.remove(herb.cleanItemId, 1)
        player.inventory.remove(defs.meta.vialOfWaterItemId, 1)
        player.inventory.addItem(herb.unfPotionItemId)
    }

    private fun makePotion(player: Player, secondaryId: Int, unfId: Int, defs: HerbloreConfig) {
        val recipe = defs.bySecondaryAndUnf[secondaryId to unfId] ?: return
        if (player.skills.getLevel(Skill.HERBLORE) < recipe.levelRequired) {
            // TODO: send "You need level X Herblore to make this potion." message via packet
            return
        }
        if (!player.inventory.contains(secondaryId)) return
        if (!player.inventory.contains(unfId)) return
        player.inventory.remove(secondaryId, 1)
        player.inventory.remove(unfId, 1)
        player.inventory.addItem(recipe.outputItemId)
        player.skills.addXp(Skill.HERBLORE, recipe.xp)
    }
}
