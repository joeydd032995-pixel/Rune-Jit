package skills.woodcutting

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Woodcutting object interactions for all trees in woodcutting.yaml.
 * All XP values, level requirements, and tick rates are loaded from the data file —
 * none are hardcoded here. Source: server-skills.md rule.
 */
class WoodcuttingPlugin(
    private val yamlPath: Path = Path.of("data/skills/woodcutting.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = WoodcuttingDefs.init(yamlPath)

        for ((_, tree) in defs.trees) {
            if (tree.objectIds.isEmpty()) continue  // object IDs pending cache extraction
            ctx.onObjectInteract("Chop down", tree.objectIds) { player, _ ->
                startChop(player, tree, defs)
            }
        }
    }

    private fun startChop(player: Player, tree: TreeDef, defs: WoodcuttingConfig) {
        val wcLevel = player.skills.getLevel(Skill.WOODCUTTING)

        if (wcLevel < tree.levelRequired) {
            // TODO: send "You need level X Woodcutting to chop this tree." message via packet
            return
        }

        val axe = selectBestAxe(player, defs.axes) ?: run {
            // TODO: send "You don't have an axe which you have the Woodcutting level to use."
            return
        }

        val action = WoodcuttingAction(player, tree, axe, player.tickQueue, defs)
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
    }

    /**
     * Selects the best axe available to the player.
     * Checks equipped weapon slot first (slot 3), then scans inventory.
     * Returns the highest-wcBonus axe the player has the Attack level to wield.
     * Source: https://oldschool.runescape.wiki/w/Axe
     */
    private fun selectBestAxe(player: Player, axes: List<AxeDef>): AxeDef? {
        val attackLevel = player.skills.getLevel(Skill.ATTACK)
        val availableAxes = axes.filter { axe ->
            val equippedInWeaponSlot = player.equipment[3] == axe.itemId
            val inInventory = player.inventory.contains(axe.itemId)
            (equippedInWeaponSlot || inInventory) && attackLevel >= axe.wieldLevel
        }
        return availableAxes.maxByOrNull { it.wcBonus }
    }
}
