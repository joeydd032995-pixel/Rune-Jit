package prayer

import combat.CombatFormulas
import combat.CombatStyle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Parity tests verifying that prayer multipliers are correctly applied to combat formulas.
 * All expected values are derived from the OSRS wiki formulas.
 *
 * Source: https://oldschool.runescape.wiki/w/Prayer
 * Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PrayerCombatMultipliersTest {

    @BeforeAll
    fun setup() {
        PrayerDefs.init()
    }

    /** Creates a PrayerSet at Prayer level 99 with full prayer points, then activates the given prayers. */
    private fun prayerSetWith(vararg prayers: Prayer): PrayerSet {
        val ps = PrayerSet { 99 }
        ps.fillPoints()
        prayers.forEach { ps.activate(it) }
        return ps
    }

    // -------------------------------------------------------------------------
    // Test 1: Piety strength multiplier applied to max melee hit
    // Piety strengthMult = 1.23
    // effective = floor(99 * 1.23) + 0 + 8 = 121 + 8 = 129 (no style bonus)
    // maxHit = (129 * (82 + 64) + 320) / 640 = (129 * 146 + 320) / 640 = 19154 / 640 = 29
    // Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit
    // Source: https://oldschool.runescape.wiki/w/Piety
    // -------------------------------------------------------------------------
    @Test fun `piety strength multiplier 1_23 applied to max melee hit at 99 strength 82 bonus`() {
        val ps = prayerSetWith(Prayer.PIETY)
        assertEquals(1.23, ps.strengthMult, 0.001,
            "Piety should provide 1.23x strength multiplier. " +
                "Source: https://oldschool.runescape.wiki/w/Piety")

        // effective = floor(99 * 1.23) + 0 + 8 = 121 + 8 = 129
        // maxHit = (129 * 146 + 320) / 640 = 19154 / 640 = 29
        val expected = CombatFormulas.maxMeleeHit(99, 82, prayerMult = 1.23)
        assertEquals(29, expected,
            "Level 99 Strength, +82 bonus, Piety (×1.23) → max hit 29. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit")
        assertEquals(expected, CombatFormulas.maxMeleeHit(99, 82, prayerMult = ps.strengthMult),
            "PrayerSet.strengthMult should match Piety value 1.23. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit")
    }

    // -------------------------------------------------------------------------
    // Test 2: Ultimate Strength multiplier = 1.15 applied to max melee hit
    // effective = floor(99 * 1.15) + 0 + 8 = 113 + 8 = 121
    // maxHit = (121 * (82 + 64) + 320) / 640 = (121 * 146 + 320) / 640 = 17986 / 640 = 28
    // Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit
    // Source: https://oldschool.runescape.wiki/w/Ultimate_Strength
    // -------------------------------------------------------------------------
    @Test fun `ultimate strength multiplier 1_15 applied to max melee hit at 99 strength 82 bonus`() {
        val ps = prayerSetWith(Prayer.ULTIMATE_STRENGTH)
        assertEquals(1.15, ps.strengthMult, 0.001,
            "Ultimate Strength should provide 1.15x strength multiplier. " +
                "Source: https://oldschool.runescape.wiki/w/Ultimate_Strength")

        // effective = floor(99 * 1.15) + 0 + 8 = 113 + 8 = 121
        // maxHit = (121 * 146 + 320) / 640 = 17986 / 640 = 28
        val expected = CombatFormulas.maxMeleeHit(99, 82, prayerMult = 1.15)
        assertEquals(28, expected,
            "Level 99 Strength, +82 bonus, Ultimate Strength (×1.15) → max hit 28. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit")
        assertEquals(expected, CombatFormulas.maxMeleeHit(99, 82, prayerMult = ps.strengthMult),
            "PrayerSet.strengthMult should match Ultimate Strength value 1.15. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit")
    }

    // -------------------------------------------------------------------------
    // Test 3: Rigour — rangedMult = 1.20, rangedStrengthMult = 1.23
    // Source: https://oldschool.runescape.wiki/w/Rigour
    // -------------------------------------------------------------------------
    @Test fun `rigour provides rangedMult 1_20 and rangedStrengthMult 1_23`() {
        val ps = prayerSetWith(Prayer.RIGOUR)
        assertEquals(1.20, ps.rangedMult, 0.001,
            "Rigour should provide 1.20x ranged attack multiplier. " +
                "Source: https://oldschool.runescape.wiki/w/Rigour")
        assertEquals(1.23, ps.rangedStrengthMult, 0.001,
            "Rigour should provide 1.23x ranged strength multiplier. " +
                "Source: https://oldschool.runescape.wiki/w/Rigour")

        // Verify applied to max ranged attack roll
        // effective = floor(99 * 1.20) + 0 + 8 = 118 + 8 = 126
        // atkRoll = 126 * (0 + 64) = 8064
        val expectedAtkRoll = CombatFormulas.maxRangedAttackRoll(99, 0, prayerMult = 1.20)
        assertEquals(expectedAtkRoll,
            CombatFormulas.maxRangedAttackRoll(99, 0, prayerMult = ps.rangedMult),
            "PrayerSet.rangedMult should apply 1.20 to ranged attack roll. " +
                "Source: https://oldschool.runescape.wiki/w/Rigour")
    }

    // -------------------------------------------------------------------------
    // Test 4: Augury — magicMult = 1.25 applied to maxMagicAttackRoll
    // effective = floor(99 * 1.25) + 9 = 123 + 9 = 132
    // atkRoll = 132 * (0 + 64) = 8448
    // Source: https://oldschool.runescape.wiki/w/Augury
    // -------------------------------------------------------------------------
    @Test fun `augury magic multiplier 1_25 applied to max magic attack roll`() {
        val ps = prayerSetWith(Prayer.AUGURY)
        assertEquals(1.25, ps.magicMult, 0.001,
            "Augury should provide 1.25x magic attack multiplier. " +
                "Source: https://oldschool.runescape.wiki/w/Augury")

        // effective = floor(99 * 1.25) + 9 = 123 + 9 = 132
        // atkRoll = 132 * (0 + 64) = 8448
        val expected = CombatFormulas.maxMagicAttackRoll(99, 0, prayerMult = 1.25)
        assertEquals(8448, expected,
            "Level 99 Magic, 0 bonus, Augury (×1.25) → max magic attack roll 8448. " +
                "Source: https://oldschool.runescape.wiki/w/Augury")
        assertEquals(expected,
            CombatFormulas.maxMagicAttackRoll(99, 0, prayerMult = ps.magicMult),
            "PrayerSet.magicMult should match Augury value 1.25. " +
                "Source: https://oldschool.runescape.wiki/w/Augury")
    }

    // -------------------------------------------------------------------------
    // Test 5: No prayer — all multipliers return 1.0 (baseline unchanged)
    // Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
    // -------------------------------------------------------------------------
    @Test fun `no prayer active returns all multipliers as 1_0`() {
        val ps = PrayerSet { 99 }
        // prayerPoints = 0, no prayers active
        assertEquals(1.0, ps.strengthMult, 0.001,
            "No prayer → strengthMult = 1.0 (no bonus). " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
        assertEquals(1.0, ps.attackMult, 0.001,
            "No prayer → attackMult = 1.0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
        assertEquals(1.0, ps.defenceMult, 0.001,
            "No prayer → defenceMult = 1.0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
        assertEquals(1.0, ps.rangedMult, 0.001,
            "No prayer → rangedMult = 1.0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
        assertEquals(1.0, ps.rangedStrengthMult, 0.001,
            "No prayer → rangedStrengthMult = 1.0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
        assertEquals(1.0, ps.magicMult, 0.001,
            "No prayer → magicMult = 1.0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")

        // Verify baseline max hit is unchanged
        val noPrayer  = CombatFormulas.maxMeleeHit(99, 82)
        val withMult1 = CombatFormulas.maxMeleeHit(99, 82, prayerMult = ps.strengthMult)
        assertEquals(noPrayer, withMult1,
            "Max melee hit with prayerMult=1.0 must equal baseline. " +
                "Source: https://oldschool.runescape.wiki/w/Maximum_melee_hit")
    }

    // -------------------------------------------------------------------------
    // Test 6: Overhead protection — isProtectedFrom MELEE when PROTECT_FROM_MELEE active
    // Source: https://oldschool.runescape.wiki/w/Protect_from_Melee
    // -------------------------------------------------------------------------
    @Test fun `isProtectedFrom melee_aggressive returns true with protect from melee active`() {
        val ps = prayerSetWith(Prayer.PROTECT_FROM_MELEE)
        assertTrue(ps.isProtectedFrom(CombatStyle.MELEE_AGGRESSIVE),
            "isProtectedFrom(MELEE_AGGRESSIVE) should be true with Protect from Melee active. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Melee")
        assertTrue(ps.isProtectedFrom(CombatStyle.MELEE_ACCURATE),
            "isProtectedFrom(MELEE_ACCURATE) should be true with Protect from Melee active. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Melee")
        assertTrue(ps.isProtectedFrom(CombatStyle.MELEE_DEFENSIVE),
            "isProtectedFrom(MELEE_DEFENSIVE) should be true with Protect from Melee active. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Melee")
        assertTrue(ps.isProtectedFrom(CombatStyle.MELEE_CONTROLLED),
            "isProtectedFrom(MELEE_CONTROLLED) should be true with Protect from Melee active. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Melee")
    }

    // -------------------------------------------------------------------------
    // Test 7: No protection prayer active — isProtectedFrom returns false
    // Source: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers
    // -------------------------------------------------------------------------
    @Test fun `isProtectedFrom returns false without overhead prayer active`() {
        val ps = prayerSetWith(Prayer.PIETY)  // no overhead
        assertFalse(ps.isProtectedFrom(CombatStyle.MELEE_AGGRESSIVE),
            "isProtectedFrom(MELEE_AGGRESSIVE) should be false without Protect from Melee. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers")
        assertFalse(ps.isProtectedFrom(CombatStyle.RANGED_RAPID),
            "isProtectedFrom(RANGED_RAPID) should be false without Protect from Ranged. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers")
        assertFalse(ps.isProtectedFrom(CombatStyle.MAGIC),
            "isProtectedFrom(MAGIC) should be false without Protect from Magic. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers")
    }

    // -------------------------------------------------------------------------
    // Test 8: Protect from Ranged protects against all ranged styles
    // Source: https://oldschool.runescape.wiki/w/Protect_from_Ranged
    // -------------------------------------------------------------------------
    @Test fun `protect from ranged covers all ranged combat styles`() {
        val ps = prayerSetWith(Prayer.PROTECT_FROM_RANGED)
        assertTrue(ps.isProtectedFrom(CombatStyle.RANGED_ACCURATE),
            "isProtectedFrom(RANGED_ACCURATE) with Protect from Ranged. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Ranged")
        assertTrue(ps.isProtectedFrom(CombatStyle.RANGED_RAPID),
            "isProtectedFrom(RANGED_RAPID) with Protect from Ranged. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Ranged")
        assertTrue(ps.isProtectedFrom(CombatStyle.RANGED_LONGRANGE),
            "isProtectedFrom(RANGED_LONGRANGE) with Protect from Ranged. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Ranged")
    }

    // -------------------------------------------------------------------------
    // Test 9: Protect from Magic protects against magic
    // Source: https://oldschool.runescape.wiki/w/Protect_from_Magic
    // -------------------------------------------------------------------------
    @Test fun `protect from magic covers magic combat style`() {
        val ps = prayerSetWith(Prayer.PROTECT_FROM_MAGIC)
        assertTrue(ps.isProtectedFrom(CombatStyle.MAGIC),
            "isProtectedFrom(MAGIC) with Protect from Magic. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Magic")
        assertFalse(ps.isProtectedFrom(CombatStyle.MELEE_ACCURATE),
            "Protect from Magic should not protect from melee. " +
                "Source: https://oldschool.runescape.wiki/w/Protect_from_Magic")
    }

    // -------------------------------------------------------------------------
    // Test 10: Mutual exclusivity — activating Piety deactivates individual prayers
    // Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
    // -------------------------------------------------------------------------
    @Test fun `activating piety deactivates ultimate strength and incredible reflexes`() {
        val ps = PrayerSet { 99 }
        ps.fillPoints()
        ps.activate(Prayer.ULTIMATE_STRENGTH)
        ps.activate(Prayer.INCREDIBLE_REFLEXES)
        assertTrue(ps.activePrayers.contains(Prayer.ULTIMATE_STRENGTH),
            "Ultimate Strength should be active before Piety. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")

        ps.activate(Prayer.PIETY)
        assertTrue(ps.activePrayers.contains(Prayer.PIETY),
            "Piety should be active after activation. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
        assertFalse(ps.activePrayers.contains(Prayer.ULTIMATE_STRENGTH),
            "Ultimate Strength should be deactivated when Piety is activated " +
                "(mutual exclusivity — both boost strength). " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
        assertFalse(ps.activePrayers.contains(Prayer.INCREDIBLE_REFLEXES),
            "Incredible Reflexes should be deactivated when Piety is activated " +
                "(mutual exclusivity — both boost attack). " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses")
    }
}
