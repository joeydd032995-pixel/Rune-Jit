package combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Path

/**
 * Parity tests for ranged combat formulas and data.
 * Expected values sourced from the OSRS wiki.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RangedFormulasTest {

    private lateinit var config: CombatConfig

    @BeforeAll fun loadConfig() {
        config = CombatLoader.load(Path.of("data/combat"))
    }

    // -------------------------------------------------------------------------
    // Max ranged hit — Source: https://oldschool.runescape.wiki/w/Ranged_strength
    // Formula: floor(0.5 + effective * (rangedStrBonus + 64) / 640)
    // effective = floor(rangedLevel * prayerMult) + styleBonus + 8
    // -------------------------------------------------------------------------

    @Test fun `max ranged hit at level 99 ranged 0 bonus no style`() {
        // effective = 99 + 0 + 8 = 107; (107 * 64 + 320) / 640 = 7168 / 640 = 11
        assertEquals(
            11,
            CombatFormulas.maxRangedHit(99, 0),
            "Level 99 Ranged, 0 bonus → max hit 11. " +
                "Source: https://oldschool.runescape.wiki/w/Ranged_strength"
        )
    }

    @Test fun `max ranged hit at level 99 ranged 112 bonus accurate style`() {
        // effective = 99 + 3 + 8 = 110 (accurate style bonus = 3)
        // (110 * 176 + 320) / 640 = 19680 / 640 = 30
        assertEquals(
            30,
            CombatFormulas.maxRangedHit(99, 112, styleBonus = 3),
            "Level 99 Ranged, +112 bonus, Accurate style → max hit 30. " +
                "Source: https://oldschool.runescape.wiki/w/Ranged_strength"
        )
    }

    // -------------------------------------------------------------------------
    // XP rates — Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience
    // -------------------------------------------------------------------------

    @Test fun `ranged xp per damage is 4`() {
        val xp = CombatFormulas.rangedXpForDamage(1)
        assertEquals(
            4.0, xp.ranged, 0.001,
            "1 damage dealt with Ranged → 4 Ranged XP. " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience"
        )
    }

    @Test fun `hp xp per damage is 4 thirds`() {
        val xp = CombatFormulas.rangedXpForDamage(3)
        assertEquals(
            4.0, xp.hp, 0.001,
            "3 damage dealt → 4.0 HP XP (4/3 per damage, 3 × 4/3 = 4). " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience"
        )
    }

    // -------------------------------------------------------------------------
    // Rapid style tick reduction — Source: https://oldschool.runescape.wiki/w/Attack_speed
    // -------------------------------------------------------------------------

    @Test fun `rapid style has ticks reduction of 1`() {
        assertEquals(
            1,
            config.ranged.attackStyles["RAPID"]!!.ticksReduction,
            "Rapid style reduces attack cycle by 1 tick. " +
                "Source: https://oldschool.runescape.wiki/w/Attack_speed"
        )
    }

    @Test fun `accurate style has no ticks reduction`() {
        assertEquals(
            0,
            config.ranged.attackStyles["ACCURATE"]!!.ticksReduction,
            "Accurate style has no tick reduction. " +
                "Source: https://oldschool.runescape.wiki/w/Attack_speed"
        )
    }

    @Test fun `shortbow base attack speed is 4 ticks`() {
        assertEquals(
            4,
            config.ranged.weaponTicks["SHORTBOW"],
            "Shortbow fires every 4 ticks. " +
                "Source: https://oldschool.runescape.wiki/w/Attack_speed"
        )
    }
}
