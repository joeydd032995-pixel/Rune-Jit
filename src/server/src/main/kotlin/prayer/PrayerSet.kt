package prayer

import combat.CombatStyle

/**
 * Per-player prayer state: active prayers, prayer points, and drain accumulator.
 *
 * Drain mechanics:
 *   - Every tick, the accumulated drain increments by the sum of all active
 *     prayers' drain_effect values.
 *   - threshold = 600 * (1 + floor(prayerBonus / 30))
 *   - Each time the accumulator reaches threshold, one prayer point is lost.
 *   - When prayer points reach 0 all prayers deactivate.
 *
 * Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
 *
 * Prayer flicking (activating and deactivating within the same tick) avoids drain
 * for that tick while still granting combat bonuses for attacks processed that tick.
 * Source: https://oldschool.runescape.wiki/w/Prayer#Prayer_flicking
 *
 * Mutual exclusivity: only one prayer per exclusive category may be active at once.
 * Activating a prayer deactivates all others in the same category first.
 * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
 */
class PrayerSet(private val maxPoints: () -> Int) {

    var prayerPoints: Int = 0
        private set

    val activePrayers: Set<Prayer> get() = _active.toSet()
    private val _active = mutableSetOf<Prayer>()
    private var drainAccumulator: Int = 0

    // -------------------------------------------------------------------------
    // Activation / deactivation
    // -------------------------------------------------------------------------

    /**
     * Activates [prayer] if the player has sufficient level and prayer points > 0.
     * Deactivates any conflicting prayers in the same exclusive category first.
     *
     * Returns true if the prayer was successfully activated; false if the player
     * lacks the required level, has no prayer points, or the prayer is already active.
     */
    fun activate(prayer: Prayer): Boolean {
        val def = PrayerDefs.get(prayer)
        if (prayerPoints <= 0) return false
        if (maxPoints() < def.levelRequired) return false
        if (_active.contains(prayer)) return false

        // Enforce mutual exclusivity: deactivate conflicting prayers
        val newCategory = prayer.category(def)
        if (newCategory != PrayerCategory.UTILITY) {
            val toDeactivate = _active.filter { active ->
                val activeDef = PrayerDefs.get(active)
                val activeCategory = active.category(activeDef)
                activeCategory == newCategory || conflictsWithCombined(prayer, def, active, activeDef)
            }
            toDeactivate.forEach { _active.remove(it) }
        }

        _active.add(prayer)
        return true
    }

    /**
     * Returns true if activating [newPrayer] should force-deactivate [existingPrayer]
     * due to combined prayer conflicts (e.g. Piety grants attack+strength+defence,
     * so activating Piety should remove Incredible Reflexes even though Piety's
     * primary category is STRENGTH).
     */
    private fun conflictsWithCombined(
        newPrayer: Prayer,
        newDef: PrayerDefinition,
        existingPrayer: Prayer,
        existingDef: PrayerDefinition,
    ): Boolean {
        // If the new prayer is a combined prayer (multiple multipliers > 1.0),
        // it overrides individual prayers in any of the categories it covers.
        val newCoversAttack   = newDef.attackMult > 1.0
        val newCoversStrength = newDef.strengthMult > 1.0
        val newCoversDefence  = newDef.defenceMult > 1.0
        val newCoversRanged   = newDef.rangedMult > 1.0 || newDef.rangedStrengthMult > 1.0
        val newCoversMagic    = newDef.magicMult > 1.0 || newDef.magicDefenceMult > 1.0

        if (existingDef.attackMult   > 1.0 && newCoversAttack)   return true
        if (existingDef.strengthMult > 1.0 && newCoversStrength) return true
        if (existingDef.defenceMult  > 1.0 && newCoversDefence)  return true
        if ((existingDef.rangedMult > 1.0 || existingDef.rangedStrengthMult > 1.0) && newCoversRanged) return true
        if ((existingDef.magicMult  > 1.0 || existingDef.magicDefenceMult  > 1.0) && newCoversMagic)  return true

        return false
    }

    /** Deactivates [prayer] if it is currently active. */
    fun deactivate(prayer: Prayer) {
        _active.remove(prayer)
    }

    /** Deactivates all active prayers and resets the drain accumulator. */
    fun deactivateAll() {
        _active.clear()
        drainAccumulator = 0
    }

    // -------------------------------------------------------------------------
    // Point management
    // -------------------------------------------------------------------------

    /**
     * Restores [amount] prayer points, clamped to the player's maximum.
     * Used by prayer potions, super restore potions, and prayer altars.
     */
    fun restorePoints(amount: Int) {
        prayerPoints = (prayerPoints + amount).coerceAtMost(maxPoints())
    }

    /**
     * Fills prayer points to maximum (player's Prayer level).
     * Used by prayer altars (e.g., the Altar in Lumbridge Castle).
     */
    fun fillPoints() {
        prayerPoints = maxPoints()
    }

    // -------------------------------------------------------------------------
    // Per-tick drain
    // -------------------------------------------------------------------------

    /**
     * Called every game tick by [PrayerDrainAction].
     * Accumulates drain and reduces prayer points when the threshold is crossed.
     *
     * Drain formula:
     *   threshold = 600 * (1 + floor(prayerBonus / 30))
     *   accumulator += sum(active drain_effects)
     *   while accumulator >= threshold: prayerPoints--, accumulator -= threshold
     *
     * Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
     *
     * @param prayerBonus Equipment prayer bonus from rings/armour (e.g., 4 for Proselyte).
     */
    fun tick(prayerBonus: Int) {
        if (_active.isEmpty() || prayerPoints <= 0) return

        val totalDrain = _active.sumOf { PrayerDefs.get(it).drainEffect }
        val threshold  = 600 * (1 + prayerBonus / 30)

        drainAccumulator += totalDrain
        while (drainAccumulator >= threshold && prayerPoints > 0) {
            drainAccumulator -= threshold
            prayerPoints--
        }

        if (prayerPoints <= 0) {
            prayerPoints = 0
            _active.clear()
            drainAccumulator = 0
        }
    }

    // -------------------------------------------------------------------------
    // Multiplier getters — return the highest active multiplier for each stat
    // -------------------------------------------------------------------------

    /**
     * Melee strength multiplier from active prayers (e.g. 1.23 for Piety).
     * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
     */
    val strengthMult: Double get() =
        _active.maxOfOrNull { PrayerDefs.get(it).strengthMult } ?: 1.0

    /**
     * Melee attack multiplier from active prayers (e.g. 1.20 for Piety).
     * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
     */
    val attackMult: Double get() =
        _active.maxOfOrNull { PrayerDefs.get(it).attackMult } ?: 1.0

    /**
     * Melee defence multiplier from active prayers (e.g. 1.25 for Piety).
     * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
     */
    val defenceMult: Double get() =
        _active.maxOfOrNull { PrayerDefs.get(it).defenceMult } ?: 1.0

    /**
     * Ranged attack multiplier from active prayers (e.g. 1.20 for Rigour).
     * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
     */
    val rangedMult: Double get() =
        _active.maxOfOrNull { PrayerDefs.get(it).rangedMult } ?: 1.0

    /**
     * Ranged strength multiplier from active prayers (e.g. 1.23 for Rigour).
     * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
     */
    val rangedStrengthMult: Double get() =
        _active.maxOfOrNull { PrayerDefs.get(it).rangedStrengthMult } ?: 1.0

    /**
     * Magic attack multiplier from active prayers (e.g. 1.25 for Augury).
     * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
     */
    val magicMult: Double get() =
        _active.maxOfOrNull { PrayerDefs.get(it).magicMult } ?: 1.0

    /**
     * Magic defence multiplier from active prayers (e.g. 1.25 for Augury).
     * Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
     */
    val magicDefenceMult: Double get() =
        _active.maxOfOrNull { PrayerDefs.get(it).magicDefenceMult } ?: 1.0

    // -------------------------------------------------------------------------
    // Overhead protection
    // -------------------------------------------------------------------------

    /**
     * Returns true if the player has an overhead protection prayer active
     * for the given [attackStyle].
     *
     * In PvM: overhead reduces incoming damage by 40%.
     * In PvP: overhead reduces incoming damage to 0.
     * Source: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers
     */
    fun isProtectedFrom(attackStyle: CombatStyle): Boolean {
        val required = when (attackStyle) {
            CombatStyle.MELEE_ACCURATE,
            CombatStyle.MELEE_AGGRESSIVE,
            CombatStyle.MELEE_DEFENSIVE,
            CombatStyle.MELEE_CONTROLLED -> OverheadProtection.MELEE

            CombatStyle.RANGED_ACCURATE,
            CombatStyle.RANGED_RAPID,
            CombatStyle.RANGED_LONGRANGE -> OverheadProtection.RANGED

            CombatStyle.MAGIC            -> OverheadProtection.MAGIC
        }
        return _active.any { PrayerDefs.get(it).overheadProtection == required }
    }
}
