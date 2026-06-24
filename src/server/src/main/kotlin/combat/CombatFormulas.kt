package combat

import engine.OsrsRandom

/**
 * Pure combat formula functions — no side effects, all integer arithmetic.
 * Every formula is floor-truncated per OSRS wiki specification.
 */
object CombatFormulas {

    // -------------------------------------------------------------------------
    // Melee
    // -------------------------------------------------------------------------

    /**
     * Max melee hit.
     * Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit
     * Formula: floor(0.5 + effective * (strengthBonus + 64) / 640)
     *        = (effective * (strengthBonus + 64) + 320) / 640  [integer arithmetic]
     * effective = floor(strengthLevel * prayerMult) + styleBonus + 8
     */
    fun maxMeleeHit(
        strengthLevel: Int,
        strengthBonus: Int,
        prayerMult: Double = 1.0,
        styleBonus: Int = 0,
    ): Int {
        val effective = (strengthLevel * prayerMult).toInt() + styleBonus + 8
        return (effective.toLong() * (strengthBonus + 64) + 320L).toInt() / 640
    }

    /**
     * Max melee attack roll.
     * Source: https://oldschool.runescape.wiki/w/Accuracy
     * effective = floor(attackLevel * prayerMult) + styleBonus + 8
     */
    fun maxMeleeAttackRoll(
        attackLevel: Int,
        attackBonus: Int,
        prayerMult: Double = 1.0,
        styleBonus: Int = 0,
    ): Int {
        val effective = (attackLevel * prayerMult).toInt() + styleBonus + 8
        return effective * (attackBonus + 64)
    }

    /**
     * Max melee defence roll.
     * Source: https://oldschool.runescape.wiki/w/Accuracy
     * effective = floor(defenceLevel * prayerMult) + 9
     */
    fun maxMeleeDefenceRoll(
        defenceLevel: Int,
        defenceBonus: Int,
        prayerMult: Double = 1.0,
    ): Int {
        val effective = (defenceLevel * prayerMult).toInt() + 9
        return effective * (defenceBonus + 64)
    }

    // -------------------------------------------------------------------------
    // Ranged
    // -------------------------------------------------------------------------

    /**
     * Max ranged hit.
     * Source: https://oldschool.runescape.wiki/w/Ranged_strength
     * Formula: floor(0.5 + effective * (rangedStrBonus + 64) / 640)
     * effective = floor(rangedLevel * prayerMult) + styleBonus + 8
     */
    fun maxRangedHit(
        rangedLevel: Int,
        rangedStrBonus: Int,
        prayerMult: Double = 1.0,
        styleBonus: Int = 0,
    ): Int {
        val effective = (rangedLevel * prayerMult).toInt() + styleBonus + 8
        return (effective.toLong() * (rangedStrBonus + 64) + 320L).toInt() / 640
    }

    /**
     * Max ranged attack roll.
     * Source: https://oldschool.runescape.wiki/w/Accuracy
     */
    fun maxRangedAttackRoll(
        rangedLevel: Int,
        rangedAttackBonus: Int,
        prayerMult: Double = 1.0,
        styleBonus: Int = 0,
    ): Int {
        val effective = (rangedLevel * prayerMult).toInt() + styleBonus + 8
        return effective * (rangedAttackBonus + 64)
    }

    // -------------------------------------------------------------------------
    // Magic
    // -------------------------------------------------------------------------

    /**
     * Max magic attack roll.
     * Source: https://oldschool.runescape.wiki/w/Accuracy#Magic_accuracy
     * effective = floor(magicLevel * prayerMult) + 9  (no style bonus for magic)
     */
    fun maxMagicAttackRoll(
        magicLevel: Int,
        magicAttackBonus: Int,
        prayerMult: Double = 1.0,
    ): Int {
        val effective = (magicLevel * prayerMult).toInt() + 9
        return effective * (magicAttackBonus + 64)
    }

    // -------------------------------------------------------------------------
    // Accuracy roll
    // -------------------------------------------------------------------------

    /**
     * Determines if an attack lands.
     * Source: https://oldschool.runescape.wiki/w/Accuracy
     * Both sides roll [0, maxRoll]; attacker wins on a strictly higher roll.
     */
    fun isAccurate(maxAttackRoll: Int, maxDefenceRoll: Int): Boolean =
        OsrsRandom.nextInt(maxAttackRoll + 1) > OsrsRandom.nextInt(maxDefenceRoll + 1)

    fun rollDamage(maxHit: Int): Int = OsrsRandom.nextInt(maxHit + 1)

    // -------------------------------------------------------------------------
    // XP grants
    // -------------------------------------------------------------------------

    /**
     * Per-skill XP granted for a single attack result.
     * Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience
     */
    data class XpGrant(
        val attack: Double = 0.0,
        val strength: Double = 0.0,
        val defence: Double = 0.0,
        val ranged: Double = 0.0,
        val magic: Double = 0.0,
        val hp: Double = 0.0,
    )

    /**
     * Melee XP for [damage] dealt under [style].
     * Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience
     * Accurate/Aggressive/Defensive: 4 XP per damage to the chosen skill + 4/3 HP XP.
     * Controlled: 4/3 XP each to Attack, Strength, and Defence + 4/3 HP XP.
     */
    fun meleeXpForDamage(damage: Int, style: CombatStyle): XpGrant {
        val hpXp = damage * (4.0 / 3.0)
        return when (style) {
            CombatStyle.MELEE_ACCURATE   -> XpGrant(attack   = damage * 4.0, hp = hpXp)
            CombatStyle.MELEE_AGGRESSIVE -> XpGrant(strength = damage * 4.0, hp = hpXp)
            CombatStyle.MELEE_DEFENSIVE  -> XpGrant(defence  = damage * 4.0, hp = hpXp)
            CombatStyle.MELEE_CONTROLLED -> {
                val split = damage * (4.0 / 3.0)
                XpGrant(attack = split, strength = split, defence = split, hp = hpXp)
            }
            else -> XpGrant()
        }
    }

    /**
     * Ranged XP for [damage] dealt.
     * Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience
     * 4 Ranged XP + 4/3 HP XP per damage point.
     */
    fun rangedXpForDamage(damage: Int): XpGrant =
        XpGrant(ranged = damage * 4.0, hp = damage * (4.0 / 3.0))

    /**
     * Magic XP for a cast that dealt [damage].
     * Source: https://oldschool.runescape.wiki/w/Magic#Magic_experience
     * Always grants [spellBaseXp]; on hit also grants 2 XP per damage + 4/3 HP XP per damage.
     */
    fun magicXpForDamage(damage: Int, spellBaseXp: Double): XpGrant =
        XpGrant(magic = spellBaseXp + damage * 2.0, hp = damage * (4.0 / 3.0))
}
