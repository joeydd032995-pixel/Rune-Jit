package skills.fishing

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One fishing cycle: checks guards, consumes bait, awards XP and fish, rolls heron pet.
 * Rescheduled every [ticksPerAttempt] ticks until the player stops or inventory fills.
 *
 * Fishing has no success roll — a fish is always caught after ticksPerAttempt ticks.
 * This differs from Woodcutting and Mining which use a per-tick success chance.
 * Source: https://oldschool.runescape.wiki/w/Fishing
 *
 * Bait consumption: one bait is consumed per catch.
 * Source: https://oldschool.runescape.wiki/w/Fishing#Bait
 *
 * Heron pet rate: 1/257,803 per catch (does not scale with level).
 * Source: https://oldschool.runescape.wiki/w/Heron#Drop_rate
 */
class FishingAction(
    private val player: Player,
    private val spot: FishingSpotDef,
    private val tickQueue: TickQueue,
    private val defs: FishingConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        // Guard: inventory full — stop fishing
        if (player.inventory.isFull()) {
            active = false
            return false
        }

        // Guard: level too low (shouldn't normally happen mid-session, but guard anyway)
        if (player.skills.getBoostedLevel(Skill.FISHING) < spot.levelRequired) {
            active = false
            return false
        }

        // Guard: bait required but not present — stop fishing
        if (spot.baitItemId != 0 && !player.inventory.contains(spot.baitItemId)) {
            // TODO: send "You need more bait to fish here." chat message via packet
            active = false
            return false
        }

        // Consume one bait per catch
        // Source: https://oldschool.runescape.wiki/w/Fishing#Bait
        if (spot.baitItemId != 0) {
            player.inventory.remove(spot.baitItemId, 1)
        }

        // Award XP — flat value, no success roll
        // Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
        player.skills.addXp(Skill.FISHING, spot.xp)

        // Add fish to inventory
        player.inventory.addItem(spot.fishItemId)

        // Roll for Heron pet
        // Source: https://oldschool.runescape.wiki/w/Heron#Drop_rate
        rollHeronPet()

        // Reschedule for next catch attempt
        tickQueue.schedule(defs.meta.ticksPerAttempt, this)
        return true
    }

    /**
     * Rolls for the Heron pet at a flat 1/[HeronPetConfig.baseRate] chance per catch.
     * The Heron drop rate does not scale with Fishing level.
     * Source: https://oldschool.runescape.wiki/w/Heron#Drop_rate
     */
    private fun rollHeronPet() {
        if (player.inventory.isFull()) return
        if (OsrsRandom.nextInt(defs.pet.baseRate) == 0) {
            player.inventory.addItem(defs.pet.itemId)
        }
    }

    fun cancel() {
        active = false
    }
}
