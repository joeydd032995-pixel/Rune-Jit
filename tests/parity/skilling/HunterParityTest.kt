package parity.skilling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import skills.hunter.HunterConfig
import skills.hunter.HunterLoader
import java.nio.file.Path

/**
 * Parity tests for Hunter skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Hunter
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HunterParityTest {

    private lateinit var config: HunterConfig

    @BeforeAll
    fun loadData() {
        config = HunterLoader.load(Path.of("data/skills/hunter.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta / config
    // -------------------------------------------------------------------------

    /**
     * Default check interval is 1 tick.
     * Source: https://oldschool.runescape.wiki/w/Hunter#Mechanics
     */
    @Test fun `default check ticks is 1`() {
        assertEquals(1, config.meta.defaultCheckTicks,
            "default_check_ticks must be 1. See: https://oldschool.runescape.wiki/w/Hunter#Mechanics")
    }

    /**
     * Catch rate resolution is 1000 (integer arithmetic scaling factor).
     * Source: https://oldschool.runescape.wiki/w/Hunter#Mechanics
     */
    @Test fun `catch rate resolution is 1000`() {
        assertEquals(1000, config.trapConfig.catchRateResolution,
            "catch_rate_resolution must be 1000. See: https://oldschool.runescape.wiki/w/Hunter#Mechanics")
    }

    /**
     * Base max traps is 1.
     * Source: https://oldschool.runescape.wiki/w/Hunter#Number_of_traps
     */
    @Test fun `base max traps is 1`() {
        assertEquals(1, config.trapConfig.baseMaxTraps,
            "base_max_traps must be 1. See: https://oldschool.runescape.wiki/w/Hunter#Number_of_traps")
    }

    // -------------------------------------------------------------------------
    // Creature count
    // -------------------------------------------------------------------------

    /**
     * Nine creatures defined: 5 bird snare + 4 box trap.
     * Source: https://oldschool.runescape.wiki/w/Hunter#Creatures
     */
    @Test fun `nine creatures loaded`() {
        assertEquals(9, config.creatures.size,
            "Expected 9 creatures. See: https://oldschool.runescape.wiki/w/Hunter#Creatures")
    }

    // -------------------------------------------------------------------------
    // Level requirements
    // Source: https://oldschool.runescape.wiki/w/Hunter#Creatures
    // -------------------------------------------------------------------------

    @Test fun `crimson swift level requirement is 1`() {
        assertEquals(1, config.creatures["CRIMSON_SWIFT"]!!.levelRequired,
            "CRIMSON_SWIFT level_required must be 1. See: https://oldschool.runescape.wiki/w/Crimson_swift")
    }

    @Test fun `golden warbler level requirement is 5`() {
        assertEquals(5, config.creatures["GOLDEN_WARBLER"]!!.levelRequired,
            "GOLDEN_WARBLER level_required must be 5. See: https://oldschool.runescape.wiki/w/Golden_warbler")
    }

    @Test fun `copper longtail level requirement is 9`() {
        assertEquals(9, config.creatures["COPPER_LONGTAIL"]!!.levelRequired,
            "COPPER_LONGTAIL level_required must be 9. See: https://oldschool.runescape.wiki/w/Copper_longtail")
    }

    @Test fun `cerulean twitch level requirement is 11`() {
        assertEquals(11, config.creatures["CERULEAN_TWITCH"]!!.levelRequired,
            "CERULEAN_TWITCH level_required must be 11. See: https://oldschool.runescape.wiki/w/Cerulean_twitch")
    }

    @Test fun `tropical wagtail level requirement is 19`() {
        assertEquals(19, config.creatures["TROPICAL_WAGTAIL"]!!.levelRequired,
            "TROPICAL_WAGTAIL level_required must be 19. See: https://oldschool.runescape.wiki/w/Tropical_wagtail")
    }

    @Test fun `ferret level requirement is 27`() {
        assertEquals(27, config.creatures["FERRET"]!!.levelRequired,
            "FERRET level_required must be 27. See: https://oldschool.runescape.wiki/w/Ferret")
    }

    @Test fun `chinchompa level requirement is 53`() {
        assertEquals(53, config.creatures["CHINCHOMPA"]!!.levelRequired,
            "CHINCHOMPA level_required must be 53. See: https://oldschool.runescape.wiki/w/Chinchompa")
    }

    @Test fun `red chinchompa level requirement is 63`() {
        assertEquals(63, config.creatures["RED_CHINCHOMPA"]!!.levelRequired,
            "RED_CHINCHOMPA level_required must be 63. See: https://oldschool.runescape.wiki/w/Red_chinchompa")
    }

    @Test fun `black chinchompa level requirement is 73`() {
        assertEquals(73, config.creatures["BLACK_CHINCHOMPA"]!!.levelRequired,
            "BLACK_CHINCHOMPA level_required must be 73. See: https://oldschool.runescape.wiki/w/Black_chinchompa")
    }

    // -------------------------------------------------------------------------
    // XP values
    // Source: https://oldschool.runescape.wiki/w/Hunter#Experience
    // -------------------------------------------------------------------------

    @Test fun `crimson swift xp is 34 0`() {
        assertEquals(34.0, config.creatures["CRIMSON_SWIFT"]!!.xp,
            "CRIMSON_SWIFT xp must be 34.0. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `golden warbler xp is 47 5`() {
        assertEquals(47.5, config.creatures["GOLDEN_WARBLER"]!!.xp,
            "GOLDEN_WARBLER xp must be 47.5. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `copper longtail xp is 61 0`() {
        assertEquals(61.0, config.creatures["COPPER_LONGTAIL"]!!.xp,
            "COPPER_LONGTAIL xp must be 61.0. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `cerulean twitch xp is 64 5`() {
        assertEquals(64.5, config.creatures["CERULEAN_TWITCH"]!!.xp,
            "CERULEAN_TWITCH xp must be 64.5. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `tropical wagtail xp is 95 8`() {
        assertEquals(95.8, config.creatures["TROPICAL_WAGTAIL"]!!.xp,
            "TROPICAL_WAGTAIL xp must be 95.8. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `ferret xp is 115 0`() {
        assertEquals(115.0, config.creatures["FERRET"]!!.xp,
            "FERRET xp must be 115.0. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `chinchompa xp is 198 0`() {
        assertEquals(198.0, config.creatures["CHINCHOMPA"]!!.xp,
            "CHINCHOMPA xp must be 198.0. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `red chinchompa xp is 265 0`() {
        assertEquals(265.0, config.creatures["RED_CHINCHOMPA"]!!.xp,
            "RED_CHINCHOMPA xp must be 265.0. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    @Test fun `black chinchompa xp is 315 0`() {
        assertEquals(315.0, config.creatures["BLACK_CHINCHOMPA"]!!.xp,
            "BLACK_CHINCHOMPA xp must be 315.0. See: https://oldschool.runescape.wiki/w/Hunter#Experience")
    }

    // -------------------------------------------------------------------------
    // Trap item IDs
    // Source: https://oldschool.runescape.wiki/w/Bird_snare, https://oldschool.runescape.wiki/w/Box_trap
    // -------------------------------------------------------------------------

    /**
     * Bird snare item ID is 10006.
     * Source: https://oldschool.runescape.wiki/w/Bird_snare
     */
    @Test fun `bird snare trap item id is 10006`() {
        val birdSnareCreature = config.creatures["CRIMSON_SWIFT"]!!
        assertEquals(10006, birdSnareCreature.trapItemId,
            "BIRD_SNARE trap_item_id must be 10006. See: https://oldschool.runescape.wiki/w/Bird_snare")
    }

    /**
     * Box trap item ID is 10008.
     * Source: https://oldschool.runescape.wiki/w/Box_trap
     */
    @Test fun `box trap item id is 10008`() {
        val boxTrapCreature = config.creatures["CHINCHOMPA"]!!
        assertEquals(10008, boxTrapCreature.trapItemId,
            "BOX_TRAP trap_item_id must be 10008. See: https://oldschool.runescape.wiki/w/Box_trap")
    }

    /**
     * Two distinct trap types registered (bird snare and box trap).
     * Source: https://oldschool.runescape.wiki/w/Hunter#Traps
     */
    @Test fun `two distinct trap types`() {
        assertEquals(2, config.byTrapItemId.size,
            "Expected 2 distinct trap types. See: https://oldschool.runescape.wiki/w/Hunter#Traps")
    }

    // -------------------------------------------------------------------------
    // Catch rate bounds (tolerance ≤ 0.001 per tests-parity.md)
    // Source: https://oldschool.runescape.wiki/w/Hunter#Mechanics
    // -------------------------------------------------------------------------

    @Test fun `crimson swift catch rate low is approximately 0 35`() {
        assertEquals(0.35, config.creatures["CRIMSON_SWIFT"]!!.catchRateLow, 0.001,
            "CRIMSON_SWIFT catch_rate_low must be ~0.35. See: https://oldschool.runescape.wiki/w/Crimson_swift")
    }

    @Test fun `crimson swift catch rate high is approximately 0 80`() {
        assertEquals(0.80, config.creatures["CRIMSON_SWIFT"]!!.catchRateHigh, 0.001,
            "CRIMSON_SWIFT catch_rate_high must be ~0.80. See: https://oldschool.runescape.wiki/w/Crimson_swift")
    }

    @Test fun `black chinchompa catch rate low is approximately 0 20`() {
        assertEquals(0.20, config.creatures["BLACK_CHINCHOMPA"]!!.catchRateLow, 0.001,
            "BLACK_CHINCHOMPA catch_rate_low must be ~0.20. See: https://oldschool.runescape.wiki/w/Black_chinchompa")
    }

    @Test fun `black chinchompa catch rate high is approximately 0 68`() {
        assertEquals(0.68, config.creatures["BLACK_CHINCHOMPA"]!!.catchRateHigh, 0.001,
            "BLACK_CHINCHOMPA catch_rate_high must be ~0.68. See: https://oldschool.runescape.wiki/w/Black_chinchompa")
    }

    // -------------------------------------------------------------------------
    // Catch rate integer scaling sanity checks
    // -------------------------------------------------------------------------

    @Test fun `crimson swift catch rate int at level 1 equals floor of low times resolution`() {
        val creature = config.creatures["CRIMSON_SWIFT"]!!
        val resolution = config.trapConfig.catchRateResolution
        val expected = (creature.catchRateLow * resolution).toInt()
        assertEquals(expected, creature.catchRateInt(1, resolution),
            "catchRateInt at level_required must equal floor(catchRateLow * resolution). " +
            "See: https://oldschool.runescape.wiki/w/Hunter#Mechanics")
    }

    @Test fun `crimson swift catch rate int at level 99 equals floor of high times resolution`() {
        val creature = config.creatures["CRIMSON_SWIFT"]!!
        val resolution = config.trapConfig.catchRateResolution
        val expected = (creature.catchRateHigh * resolution).toInt()
        assertEquals(expected, creature.catchRateInt(99, resolution),
            "catchRateInt at level 99 must equal floor(catchRateHigh * resolution). " +
            "See: https://oldschool.runescape.wiki/w/Hunter#Mechanics")
    }

    // -------------------------------------------------------------------------
    // Pet
    // Source: https://oldschool.runescape.wiki/w/Baby_chinchompa
    // -------------------------------------------------------------------------

    /**
     * Baby chinchompa pet item ID is 13323.
     * Source: https://oldschool.runescape.wiki/w/Baby_chinchompa
     */
    @Test fun `pet item id is 13323`() {
        assertEquals(13323, config.pet.itemId,
            "pet item_id must be 13323. See: https://oldschool.runescape.wiki/w/Baby_chinchompa")
    }

    /**
     * Pet scales with level.
     * Source: https://oldschool.runescape.wiki/w/Baby_chinchompa
     */
    @Test fun `pet scales with level`() {
        assertTrue(config.pet.scalesWithLevel,
            "pet scales_with_level must be true. See: https://oldschool.runescape.wiki/w/Baby_chinchompa")
    }

    // -------------------------------------------------------------------------
    // Trap groupings
    // -------------------------------------------------------------------------

    /**
     * Five creatures use BIRD_SNARE (Crimson swift, Golden warbler, Copper longtail, Cerulean twitch, Tropical wagtail).
     * Source: https://oldschool.runescape.wiki/w/Hunter#Creatures
     */
    @Test fun `five creatures use bird snare`() {
        val birdSnareCreatures = config.byTrapItemId[10006]
        assertEquals(5, birdSnareCreatures?.size,
            "Expected 5 BIRD_SNARE creatures. See: https://oldschool.runescape.wiki/w/Hunter#Creatures")
    }

    /**
     * Four creatures use BOX_TRAP (Ferret, Chinchompa, Red chinchompa, Black chinchompa).
     * Source: https://oldschool.runescape.wiki/w/Hunter#Creatures
     */
    @Test fun `four creatures use box trap`() {
        val boxTrapCreatures = config.byTrapItemId[10008]
        assertEquals(4, boxTrapCreatures?.size,
            "Expected 4 BOX_TRAP creatures. See: https://oldschool.runescape.wiki/w/Hunter#Creatures")
    }
}
