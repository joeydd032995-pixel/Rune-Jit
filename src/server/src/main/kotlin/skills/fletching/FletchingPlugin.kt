package skills.fletching

import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Fletching item-use interactions from fletching.yaml.
 * All XP, levels, and item IDs come from the YAML — none hardcoded here.
 * Source: https://oldschool.runescape.wiki/w/Fletching
 */
class FletchingPlugin(
    private val yamlPath: Path = Path.of("data/skills/fletching.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = FletchingDefs.init(yamlPath)

        for ((pair, recipe) in defs.byItemPair) {
            val (primary, target) = pair
            if (primary == 0 || target == 0) continue
            ctx.onItemUseOnItem(intArrayOf(primary), intArrayOf(target)) { player, _, _ ->
                val level = player.skills.getBoostedLevel(Skill.FLETCHING)
                if (level < recipe.levelRequired) return@onItemUseOnItem
                val action = FletchingAction(player, recipe, player.tickQueue, defs)
                player.tickQueue.schedule(defs.meta.ticksPerAction, action)
            }
        }
    }
}
