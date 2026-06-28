package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import skills.agility.AgilityConfig
import skills.agility.AgilityLoader
import java.nio.file.Path

/**
 * Parity tests for Agility skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Agility
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgilityParityTest {

    private lateinit var config: AgilityConfig

    @BeforeAll
    fun loadData() {
        config = AgilityLoader.load(Path.of("data/skills/agility.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * Default 2 ticks per obstacle.
     * Source: https://oldschool.runescape.wiki/w/Agility
     */
    @Test fun `default ticks per obstacle is 2`() {
        assertEquals(2, config.meta.defaultTicks,
            "default_ticks must be 2. See: https://oldschool.runescape.wiki/w/Agility")
    }

    /**
     * Mark of Grace item ID is 11849.
     * Source: https://oldschool.runescape.wiki/w/Mark_of_grace
     */
    @Test fun `mark of grace item id is 11849`() {
        assertEquals(11849, config.meta.markOfGraceItemId,
            "mark_of_grace_item_id must be 11849. See: https://oldschool.runescape.wiki/w/Mark_of_grace")
    }

    // -------------------------------------------------------------------------
    // Course count
    // -------------------------------------------------------------------------

    @Test fun `ten courses loaded`() {
        assertEquals(10, config.courses.size,
            "Expected 10 Agility courses. See: https://oldschool.runescape.wiki/w/Agility")
    }

    // -------------------------------------------------------------------------
    // Course level requirements
    // Source: https://oldschool.runescape.wiki/w/Agility#Agility_courses
    // -------------------------------------------------------------------------

    @Test fun `gnome stronghold level requirement is 1`() {
        assertEquals(1, config.courses["GNOME_STRONGHOLD"]!!.levelRequired,
            "GNOME_STRONGHOLD level_required must be 1. See: https://oldschool.runescape.wiki/w/Gnome_Stronghold_Agility_Course")
    }

    @Test fun `draynor rooftop level requirement is 10`() {
        assertEquals(10, config.courses["DRAYNOR_ROOFTOP"]!!.levelRequired,
            "DRAYNOR_ROOFTOP level_required must be 10. See: https://oldschool.runescape.wiki/w/Draynor_Village_Rooftop_Course")
    }

    @Test fun `al kharid rooftop level requirement is 20`() {
        assertEquals(20, config.courses["AL_KHARID_ROOFTOP"]!!.levelRequired,
            "AL_KHARID_ROOFTOP level_required must be 20. See: https://oldschool.runescape.wiki/w/Al_Kharid_Rooftop_Course")
    }

    @Test fun `varrock rooftop level requirement is 30`() {
        assertEquals(30, config.courses["VARROCK_ROOFTOP"]!!.levelRequired,
            "VARROCK_ROOFTOP level_required must be 30. See: https://oldschool.runescape.wiki/w/Varrock_Rooftop_Course")
    }

    @Test fun `canifis rooftop level requirement is 40`() {
        assertEquals(40, config.courses["CANIFIS_ROOFTOP"]!!.levelRequired,
            "CANIFIS_ROOFTOP level_required must be 40. See: https://oldschool.runescape.wiki/w/Canifis_Rooftop_Course")
    }

    @Test fun `falador rooftop level requirement is 50`() {
        assertEquals(50, config.courses["FALADOR_ROOFTOP"]!!.levelRequired,
            "FALADOR_ROOFTOP level_required must be 50. See: https://oldschool.runescape.wiki/w/Falador_Rooftop_Course")
    }

    @Test fun `seers village rooftop level requirement is 60`() {
        assertEquals(60, config.courses["SEERS_VILLAGE_ROOFTOP"]!!.levelRequired,
            "SEERS_VILLAGE_ROOFTOP level_required must be 60. See: https://oldschool.runescape.wiki/w/Seers%27_Village_Rooftop_Course")
    }

    @Test fun `pollnivneach rooftop level requirement is 70`() {
        assertEquals(70, config.courses["POLLNIVNEACH_ROOFTOP"]!!.levelRequired,
            "POLLNIVNEACH_ROOFTOP level_required must be 70. See: https://oldschool.runescape.wiki/w/Pollnivneach_Rooftop_Course")
    }

    @Test fun `rellekka rooftop level requirement is 80`() {
        assertEquals(80, config.courses["RELLEKKA_ROOFTOP"]!!.levelRequired,
            "RELLEKKA_ROOFTOP level_required must be 80. See: https://oldschool.runescape.wiki/w/Rellekka_Rooftop_Course")
    }

    @Test fun `ardougne rooftop level requirement is 90`() {
        assertEquals(90, config.courses["ARDOUGNE_ROOFTOP"]!!.levelRequired,
            "ARDOUGNE_ROOFTOP level_required must be 90. See: https://oldschool.runescape.wiki/w/Ardougne_Rooftop_Course")
    }

    // -------------------------------------------------------------------------
    // Lap completion bonus XP
    // Source: https://oldschool.runescape.wiki/w/Agility#Rooftop_courses
    // -------------------------------------------------------------------------

    @Test fun `gnome stronghold completion bonus xp is 39 5`() {
        assertEquals(39.5, config.courses["GNOME_STRONGHOLD"]!!.completionBonusXp,
            "GNOME_STRONGHOLD completion_bonus_xp must be 39.5. See: https://oldschool.runescape.wiki/w/Gnome_Stronghold_Agility_Course")
    }

    @Test fun `draynor rooftop completion bonus xp is 79 0`() {
        assertEquals(79.0, config.courses["DRAYNOR_ROOFTOP"]!!.completionBonusXp,
            "DRAYNOR_ROOFTOP completion_bonus_xp must be 79.0. See: https://oldschool.runescape.wiki/w/Draynor_Village_Rooftop_Course")
    }

    @Test fun `falador rooftop completion bonus xp is 440 0`() {
        assertEquals(440.0, config.courses["FALADOR_ROOFTOP"]!!.completionBonusXp,
            "FALADOR_ROOFTOP completion_bonus_xp must be 440.0. See: https://oldschool.runescape.wiki/w/Falador_Rooftop_Course")
    }

    @Test fun `pollnivneach rooftop completion bonus xp is 890 0`() {
        assertEquals(890.0, config.courses["POLLNIVNEACH_ROOFTOP"]!!.completionBonusXp,
            "POLLNIVNEACH_ROOFTOP completion_bonus_xp must be 890.0. See: https://oldschool.runescape.wiki/w/Pollnivneach_Rooftop_Course")
    }

    @Test fun `ardougne rooftop completion bonus xp is 529 0`() {
        assertEquals(529.0, config.courses["ARDOUGNE_ROOFTOP"]!!.completionBonusXp,
            "ARDOUGNE_ROOFTOP completion_bonus_xp must be 529.0. See: https://oldschool.runescape.wiki/w/Ardougne_Rooftop_Course")
    }

    // -------------------------------------------------------------------------
    // Mark of Grace chances
    // Source: https://oldschool.runescape.wiki/w/Mark_of_grace
    // -------------------------------------------------------------------------

    @Test fun `gnome stronghold has no mark of grace`() {
        assertNull(config.courses["GNOME_STRONGHOLD"]!!.markOfGraceChance,
            "GNOME_STRONGHOLD mark_of_grace_chance must be null (no marks). See: https://oldschool.runescape.wiki/w/Mark_of_grace")
    }

    @Test fun `draynor rooftop mark of grace chance is 4`() {
        assertEquals(4, config.courses["DRAYNOR_ROOFTOP"]!!.markOfGraceChance,
            "DRAYNOR_ROOFTOP mark_of_grace_chance must be 4. See: https://oldschool.runescape.wiki/w/Mark_of_grace")
    }

    @Test fun `ardougne rooftop mark of grace chance is 5`() {
        assertEquals(5, config.courses["ARDOUGNE_ROOFTOP"]!!.markOfGraceChance,
            "ARDOUGNE_ROOFTOP mark_of_grace_chance must be 5. See: https://oldschool.runescape.wiki/w/Mark_of_grace")
    }

    // -------------------------------------------------------------------------
    // Obstacle counts
    // Source: https://oldschool.runescape.wiki/w/Agility#Agility_courses
    // -------------------------------------------------------------------------

    @Test fun `gnome stronghold has 7 obstacles`() {
        assertEquals(7, config.courses["GNOME_STRONGHOLD"]!!.obstacles.size,
            "GNOME_STRONGHOLD must have 7 obstacles. See: https://oldschool.runescape.wiki/w/Gnome_Stronghold_Agility_Course")
    }

    @Test fun `ardougne rooftop has 7 obstacles`() {
        assertEquals(7, config.courses["ARDOUGNE_ROOFTOP"]!!.obstacles.size,
            "ARDOUGNE_ROOFTOP must have 7 obstacles. See: https://oldschool.runescape.wiki/w/Ardougne_Rooftop_Course")
    }

    // -------------------------------------------------------------------------
    // Obstacle XP values
    // Source: https://oldschool.runescape.wiki/w/Gnome_Stronghold_Agility_Course
    // -------------------------------------------------------------------------

    @Test fun `gnome stronghold log balance xp is 7 5`() {
        val obs = config.courses["GNOME_STRONGHOLD"]!!.obstacles.first { it.name == "LOG_BALANCE" }
        assertEquals(7.5, obs.xp,
            "LOG_BALANCE xp must be 7.5. See: https://oldschool.runescape.wiki/w/Gnome_Stronghold_Agility_Course")
    }

    @Test fun `ardougne rooftop wooden beams xp is 52 0`() {
        val obs = config.courses["ARDOUGNE_ROOFTOP"]!!.obstacles.first { it.name == "WOODEN_BEAMS" }
        assertEquals(52.0, obs.xp,
            "WOODEN_BEAMS xp must be 52.0. See: https://oldschool.runescape.wiki/w/Ardougne_Rooftop_Course")
    }

    // -------------------------------------------------------------------------
    // Pet
    // Source: https://oldschool.runescape.wiki/w/Giant_squirrel
    // -------------------------------------------------------------------------

    @Test fun `giant squirrel item id is 20659`() {
        assertEquals(20659, config.pet.itemId,
            "Giant Squirrel item_id must be 20659. See: https://oldschool.runescape.wiki/w/Giant_squirrel")
    }

    // -------------------------------------------------------------------------
    // Object IDs — all zero pending cache extraction
    // -------------------------------------------------------------------------

    @Test fun `all obstacle object ids are 0 pending cache extraction`() {
        val nonZero = config.courses.values.flatMap { it.obstacles }.filter { it.objectId != 0 }
        assertTrue(nonZero.isEmpty(),
            "All object_ids should be 0 pending /load-osrs-cache-full. " +
            "See: https://oldschool.runescape.wiki/w/Agility")
    }
}
