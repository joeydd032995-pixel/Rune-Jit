package combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Parity tests for melee combat formulas.
 * All expected values are sourced from the OSRS wiki and verified against
 * integer arithmetic per server-combat.md rules.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MeleeFormulasTest {

    // -------------------------------------------------------------------------
    // Max melee hit — Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit
    // Formula: floor(0.5 + effective * (strengthBonus + 64) / 640)
    //        = (effective * (strengthBonus + 64) + 320) / 640  [integer]
    // effective = floor(strengthLevel * prayerMult) + styleBonus + 8
    // -------------------------------------------------------------------------

    @Test fun `max melee hit at level 1 strength 0 bonus`() {
        // effective = 1 + 0 + 8 = 9; (9 * 64 + 320) / 640 = 896 / 640 = 1
        assertEquals(
            1,
            CombatFormulas.maxMeleeHit(1, 0),
            "Level 1 Strength, 0 bonus → max hit 1. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit"
        )
    }

    @Test fun `max melee hit at level 99 strength 0 bonus`() {
        // effective = 99 + 0 + 8 = 107; (107 * 64 + 320) / 640 = 7168 / 640 = 11
        assertEquals(
            11,
            CombatFormulas.maxMeleeHit(99, 0),
            "Level 99 Strength, 0 bonus → max hit 11. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit"
        )
    }

    @Test fun `max melee hit at level 99 strength 82 bonus aggressive style`() {
        // effective = 99 + 3 + 8 = 110 (aggressive style bonus = 3)
        // (110 * 146 + 320) / 640 = 16380 / 640 = 25
        assertEquals(
            25,
            CombatFormulas.maxMeleeHit(99, 82, styleBonus = 3),
            "Level 99 Strength, +82 strength bonus (whip), aggressive style → max hit 25. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit"
        )
    }

    @Test fun `max melee hit with piety prayer multiplier 1_23 at 99 strength 82 bonus`() {
        // effective = floor(99 * 1.23) + 0 + 8 = 121 + 8 = 129
        // (129 * 146 + 320) / 640 = 19154 / 640 = 29
        assertEquals(
            29,
            CombatFormulas.maxMeleeHit(99, 82, prayerMult = 1.23),
            "Level 99 Strength, +82 bonus, Piety (×1.23) → max hit 29. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit"
        )
    }

    // -------------------------------------------------------------------------
    // XP grants — Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience
    // -------------------------------------------------------------------------

    @Test fun `accurate style awards attack xp at 4 per damage point`() {
        val xp = CombatFormulas.meleeXpForDamage(10, CombatStyle.MELEE_ACCURATE)
        assertEquals(
            40.0, xp.attack, 0.001,
            "10 damage with Accurate style → 40 Attack XP (4 per damage). " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience"
        )
        assertEquals(
            0.0, xp.strength, 0.001,
            "Accurate style awards 0 Strength XP"
        )
    }

    @Test fun `accurate style awards hp xp at 4 thirds per damage point`() {
        val xp = CombatFormulas.meleeXpForDamage(10, CombatStyle.MELEE_ACCURATE)
        assertEquals(
            10 * (4.0 / 3.0), xp.hp, 0.001,
            "10 damage → 13.33 HP XP (4/3 per damage). " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience"
        )
    }

    @Test fun `aggressive style awards strength xp at 4 per damage point`() {
        val xp = CombatFormulas.meleeXpForDamage(10, CombatStyle.MELEE_AGGRESSIVE)
        assertEquals(40.0, xp.strength, 0.001,
            "10 damage with Aggressive → 40 Strength XP. " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience")
        assertEquals(0.0, xp.attack, 0.001, "Aggressive style awards 0 Attack XP")
    }

    @Test fun `defensive style awards defence xp at 4 per damage point`() {
        val xp = CombatFormulas.meleeXpForDamage(10, CombatStyle.MELEE_DEFENSIVE)
        assertEquals(40.0, xp.defence, 0.001,
            "10 damage with Defensive → 40 Defence XP. " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience")
    }

    @Test fun `controlled style splits xp evenly across attack strength and defence`() {
        val xp = CombatFormulas.meleeXpForDamage(9, CombatStyle.MELEE_CONTROLLED)
        val expected = 9 * (4.0 / 3.0)
        assertEquals(expected, xp.attack,   0.001,
            "Controlled style: each of Attack/Strength/Defence gets 4/3 XP per damage. " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience")
        assertEquals(expected, xp.strength, 0.001,
            "Controlled style: Strength XP = 4/3 per damage")
        assertEquals(expected, xp.defence,  0.001,
            "Controlled style: Defence XP = 4/3 per damage")
        assertEquals(expected, xp.hp,       0.001,
            "Controlled style: HP XP = 4/3 per damage")
    }

    // -------------------------------------------------------------------------
    // Attack & defence rolls — Source: https://oldschool.runescape.wiki/w/Accuracy
    // -------------------------------------------------------------------------

    @Test fun `max melee attack roll at 99 attack 0 bonus no style`() {
        // effective = 99 + 0 + 8 = 107; roll = 107 * (0 + 64) = 6848
        assertEquals(
            6848,
            CombatFormulas.maxMeleeAttackRoll(99, 0),
            "Level 99 Attack, 0 bonus → max attack roll 6848. " +
                "Source: https://oldschool.runescape.wiki/w/Accuracy"
        )
    }

    @Test fun `max melee defence roll at 99 defence 0 bonus`() {
        // effective = 99 + 9 = 108 (defence always +9); roll = 108 * 64 = 6912
        assertEquals(
            6912,
            CombatFormulas.maxMeleeDefenceRoll(99, 0),
            "Level 99 Defence, 0 bonus → max defence roll 6912. " +
                "Source: https://oldschool.runescape.wiki/w/Accuracy"
        )
    }
}
