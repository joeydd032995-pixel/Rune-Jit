package skills.construction

import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Construction hotspot build interactions.
 * All XP values, level requirements, and material requirements are loaded from
 * data/skills/construction.yaml — nothing is hardcoded here.
 *
 * Hotspot object IDs are all 0 (pending cache extraction), so all registrations
 * are skipped at startup — matching the FishingPlugin empty-id guard pattern.
 *
 * Source: https://oldschool.runescape.wiki/w/Construction
 */
class ConstructionPlugin(
    private val yamlPath: Path = Path.of("data/skills/construction.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = ConstructionDefs.init(yamlPath)

        for ((_, furniture) in defs.furniture) {
            // Skip registration when hotspot object ID not yet available from cache
            if (furniture.hotspotObjectId == 0) continue

            ctx.onObjectInteract("Build", intArrayOf(furniture.hotspotObjectId)) { player, _ ->
                val level = player.skills.getLevel(Skill.CONSTRUCTION)
                if (level < furniture.levelRequired) return@onObjectInteract

                // Saw and hammer are required tools (not consumed)
                if (!player.inventory.contains(defs.meta.toolSawItemId) &&
                    !player.equipment.contains(defs.meta.toolSawItemId)) return@onObjectInteract
                if (!player.inventory.contains(defs.meta.toolHammerItemId) &&
                    !player.equipment.contains(defs.meta.toolHammerItemId)) return@onObjectInteract

                // Pre-check all materials before scheduling
                for (mat in furniture.materials) {
                    if (!player.inventory.contains(mat.itemId, mat.quantity)) return@onObjectInteract
                }

                val action = ConstructionAction(player, furniture, defs)
                player.tickQueue.schedule(defs.meta.ticksPerBuild, action)
            }
        }
    }
}
