package skills.cooking

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One cooking cycle: consumes one raw food item, rolls the burn chance, awards
 * Cooking XP and the cooked item on success or the burnt item on failure.
 * Rescheduled every [CookingMeta.ticksPerAttempt] ticks until the player runs out
 * of raw food, the inventory has no room for the result, or the player stops.
 *
 * Burn mechanic — Source: https://oldschool.runescape.wiki/w/Cooking#Burn_level
 *   - Each food has a "stop-burn" level above which it never burns. Cooking on a
 *     range burns less than on a fire, so the range stop-burn level is lower.
 *   - Cooking gauntlets (item 775) further lower the stop-burn level for lobster,
 *     swordfish and shark.
 *   - Below the stop-burn level the burn chance scales linearly from the food's
 *     minimum cook level (max burn) down to zero at the stop-burn level.
 * Source: https://oldschool.runescape.wiki/w/Cooking_gauntlets
 *
 * RNG is server-authoritative via [OsrsRandom] per server-tick.md.
 */
class CookingAction(
    private val player: Player,
    private val food: FoodDef,
    /** true when cooking on a range; false for a fire. Ranges burn less. */
    private val onRange: Boolean,
    private val tickQueue: TickQueue,
    private val defs: CookingConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        // Guard: out of raw food — stop cooking.
        if (!player.inventory.contains(food.rawItemId)) {
            active = false
            return false
        }

        // Guard: level too low (shouldn't normally happen mid-session, but guard anyway).
        if (player.skills.getBoostedLevel(Skill.COOKING) < food.levelRequired) {
            active = false
            return false
        }

        // Consume one raw item per attempt.
        player.inventory.remove(food.rawItemId, 1)

        if (rollSuccess()) {
            // Award XP only on a successful (non-burnt) cook.
            // Source: https://oldschool.runescape.wiki/w/Cooking#Cooking_food
            player.skills.addXp(Skill.COOKING, food.xp)
            player.inventory.addItem(food.cookedItemId)
        } else {
            // Burnt food: no XP, gives the burnt item instead.
            player.inventory.addItem(food.burntItemId)
        }

        // Stop if the inventory is now full (no room for the next result).
        if (player.inventory.isFull()) {
            active = false
            return false
        }

        tickQueue.schedule(defs.meta.ticksPerAttempt, this)
        return true
    }

    /**
     * Returns true if the food cooks successfully (does not burn).
     *
     * At or above the effective stop-burn level the food never burns. Below it,
     * burn chance scales linearly from the food's minimum cook level (max burn)
     * down to zero at the stop-burn level, evaluated with a 0..255 roll to match
     * OSRS integer RNG granularity.
     * Source: https://oldschool.runescape.wiki/w/Cooking#Burn_level
     */
    private fun rollSuccess(): Boolean {
        val level = player.skills.getBoostedLevel(Skill.COOKING)
        val stopBurnLevel = effectiveStopBurnLevel()

        if (level >= stopBurnLevel) return true

        // Linear burn chance: fraction of the way from the cook level to stop-burn.
        // burnChance255 = 255 * (stopBurnLevel - level) / (stopBurnLevel - cookLevel)
        val span = (stopBurnLevel - food.levelRequired).coerceAtLeast(1)
        val progress = (stopBurnLevel - level).coerceIn(0, span)
        val burnChance255 = (255 * progress) / span

        // success when the roll is at or above the burn chance.
        return OsrsRandom.nextInt(256) >= burnChance255
    }

    /**
     * The stop-burn level that applies to this cook, picking range vs fire and
     * applying Cooking gauntlets when the player wears them and the food benefits.
     * Source: https://oldschool.runescape.wiki/w/Cooking_gauntlets
     */
    private fun effectiveStopBurnLevel(): Int {
        if (food.usesGauntlets && food.gauntletsStopBurnLevel != null && wearingCookingGauntlets()) {
            return food.gauntletsStopBurnLevel
        }
        return if (onRange) food.stopBurnLevelRange else food.stopBurnLevelFire
    }

    /** Cooking gauntlets occupy the hands slot (index 9). */
    private fun wearingCookingGauntlets(): Boolean =
        player.equipment.any { it == defs.meta.cookingGauntletsItemId }

    fun cancel() {
        active = false
    }
}
