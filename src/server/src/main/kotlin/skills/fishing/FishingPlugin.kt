package skills.fishing

import entity.Player
import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Fishing NPC interactions for all spots defined in fishing.yaml.
 * All XP values, level requirements, bait IDs, and tick rates are loaded from the data file —
 * none are hardcoded here. Source: server-skills.md rule.
 *
 * Right-click option mapping (OSRS wiki):
 *   SMALL_NET  → "Net"
 *   BIG_NET    → "Net"
 *   BAIT       → "Bait"
 *   FLY_FISH   → "Lure"
 *   CAGE       → "Cage"
 *   HARPOON    → "Harpoon"
 *   SANDWORMS  → "Sandworms"
 *   LOBSTER_POT → "Cage"
 * Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_spots
 */
class FishingPlugin(
    private val yamlPath: Path = Path.of("data/skills/fishing.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = FishingDefs.init(yamlPath)

        for ((_, spot) in defs.spots) {
            if (spot.objectIds.isEmpty()) continue  // object IDs pending cache extraction

            val option = methodToOption(spot.method)
            ctx.onObjectInteract(option, spot.objectIds) { player, _ ->
                startFishing(player, spot, defs)
            }
        }
    }

    private fun startFishing(player: Player, spot: FishingSpotDef, defs: FishingConfig) {
        val fishingLevel = player.skills.getLevel(Skill.FISHING)

        if (fishingLevel < spot.levelRequired) {
            // TODO: send "You need level X Fishing to fish here." message via packet
            return
        }

        // Check tool is in inventory (fishing tools are not wielded like axes/pickaxes)
        if (!player.inventory.contains(spot.toolItemId)) {
            // TODO: send "You need a [tool] to fish here." message via packet
            return
        }

        val action = FishingAction(player, spot, player.tickQueue, defs)
        player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
    }

    /**
     * Maps a [FishingMethod] to the right-click option string shown on fishing spot NPCs.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_spots
     */
    private fun methodToOption(method: FishingMethod): String = when (method) {
        FishingMethod.SMALL_NET   -> "Net"
        FishingMethod.BIG_NET     -> "Net"
        FishingMethod.BAIT        -> "Bait"
        FishingMethod.FLY_FISH    -> "Lure"
        FishingMethod.CAGE        -> "Cage"
        FishingMethod.HARPOON     -> "Harpoon"
        FishingMethod.SANDWORMS   -> "Sandworms"
        FishingMethod.LOBSTER_POT -> "Cage"
    }
}
