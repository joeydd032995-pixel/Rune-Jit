package combat

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Path

/**
 * Parity tests for magic combat data and formulas.
 * Spell level requirements and max hit values loaded from data/combat/magic.yaml
 * and verified against the OSRS wiki.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MagicFormulasTest {

    private lateinit var config: CombatConfig

    @BeforeAll fun loadConfig() {
        config = CombatLoader.load(Path.of("data/combat"))
    }

    // -------------------------------------------------------------------------
    // Standard spellbook — Source: https://oldschool.runescape.wiki/w/Standard_spellbook
    // -------------------------------------------------------------------------

    /** Source: https://oldschool.runescape.wiki/w/Fire_Strike */
    @Test fun `fire strike level required is 13`() {
        assertEquals(
            13,
            config.standardBook.spells["FIRE_STRIKE"]!!.levelRequired,
            "Fire Strike requires Magic level 13. " +
                "Source: https://oldschool.runescape.wiki/w/Fire_Strike"
        )
    }

    /** Source: https://oldschool.runescape.wiki/w/Fire_Strike */
    @Test fun `fire strike max hit is 8`() {
        assertEquals(
            8,
            config.standardBook.spells["FIRE_STRIKE"]!!.maxHit,
            "Fire Strike max hit is 8. " +
                "Source: https://oldschool.runescape.wiki/w/Fire_Strike"
        )
    }

    /** Source: https://oldschool.runescape.wiki/w/Ice_Barrage */
    @Test fun `ice barrage level required is 94`() {
        assertEquals(
            94,
            config.standardBook.spells["ICE_BARRAGE"]!!.levelRequired,
            "Ice Barrage requires Magic level 94. " +
                "Source: https://oldschool.runescape.wiki/w/Ice_Barrage"
        )
    }

    /** Source: https://oldschool.runescape.wiki/w/Ice_Barrage */
    @Test fun `ice barrage max hit is 30`() {
        assertEquals(
            30,
            config.standardBook.spells["ICE_BARRAGE"]!!.maxHit,
            "Ice Barrage max hit is 30. " +
                "Source: https://oldschool.runescape.wiki/w/Ice_Barrage"
        )
    }

    /** Source: https://oldschool.runescape.wiki/w/Ice_Barrage */
    @Test fun `ice barrage freeze duration is 32 ticks`() {
        assertEquals(
            32,
            config.standardBook.spells["ICE_BARRAGE"]!!.freezeTicks,
            "Ice Barrage freezes for 32 ticks (20 seconds). " +
                "Source: https://oldschool.runescape.wiki/w/Ice_Barrage"
        )
    }

    /** Source: https://oldschool.runescape.wiki/w/Attack_speed */
    @Test fun `magic combat cycle is 5 ticks`() {
        assertEquals(
            5,
            config.standardBook.combatCycleTicks,
            "Magic combat cycle is 5 ticks (3 seconds). " +
                "Source: https://oldschool.runescape.wiki/w/Attack_speed"
        )
    }

    // -------------------------------------------------------------------------
    // XP formula — Source: https://oldschool.runescape.wiki/w/Magic#Magic_experience
    // -------------------------------------------------------------------------

    @Test fun `magic xp on miss grants only base spell xp`() {
        val xp = CombatFormulas.magicXpForDamage(0, 11.5) // Fire Strike miss
        assertEquals(
            11.5, xp.magic, 0.001,
            "On a miss (0 damage), Fire Strike grants 11.5 base Magic XP. " +
                "Source: https://oldschool.runescape.wiki/w/Magic#Magic_experience"
        )
        assertEquals(0.0, xp.hp, 0.001,
            "On a miss, no HP XP is granted")
    }

    @Test fun `magic xp on hit grants base xp plus 2 per damage point`() {
        val xp = CombatFormulas.magicXpForDamage(8, 11.5) // Fire Strike max hit
        assertEquals(
            11.5 + 8 * 2.0, xp.magic, 0.001,
            "Fire Strike hitting max (8 damage) → 11.5 + 16 = 27.5 Magic XP. " +
                "Source: https://oldschool.runescape.wiki/w/Magic#Magic_experience"
        )
        assertEquals(
            8 * (4.0 / 3.0), xp.hp, 0.001,
            "8 damage → 10.67 HP XP (4/3 per damage). " +
                "Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience"
        )
    }
}
