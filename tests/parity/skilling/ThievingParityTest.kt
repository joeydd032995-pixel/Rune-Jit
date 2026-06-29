package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import skills.thieving.ThievingConfig
import skills.thieving.ThievingLoader
import java.nio.file.Path

/**
 * Parity tests for Thieving skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Thieving
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThievingParityTest {

    private lateinit var config: ThievingConfig

    @BeforeAll
    fun loadData() {
        config = ThievingLoader.load(Path.of("data/skills/thieving.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * 4 ticks per pickpocket attempt (2.4 seconds).
     * Source: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing
     */
    @Test fun `ticks per attempt is 4`() {
        assertEquals(4, config.meta.ticksPerAttempt,
            "ticks_per_attempt must be 4. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    // -------------------------------------------------------------------------
    // Pickpocket counts and targets
    // -------------------------------------------------------------------------

    @Test fun `seven pickpocket targets loaded`() {
        assertEquals(7, config.pickpockets.size,
            "Expected 7 pickpocket targets. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    // -------------------------------------------------------------------------
    // Pickpocket level requirements
    // Source: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing
    // -------------------------------------------------------------------------

    @Test fun `man level requirement is 1`() {
        assertEquals(1, config.pickpockets["MAN"]!!.levelRequired,
            "MAN level_required must be 1. See: https://oldschool.runescape.wiki/w/Man")
    }

    @Test fun `farmer level requirement is 10`() {
        assertEquals(10, config.pickpockets["FARMER"]!!.levelRequired,
            "FARMER level_required must be 10. See: https://oldschool.runescape.wiki/w/Farmer")
    }

    @Test fun `master farmer level requirement is 38`() {
        assertEquals(38, config.pickpockets["MASTER_FARMER"]!!.levelRequired,
            "MASTER_FARMER level_required must be 38. See: https://oldschool.runescape.wiki/w/Master_Farmer")
    }

    @Test fun `guard level requirement is 40`() {
        assertEquals(40, config.pickpockets["GUARD"]!!.levelRequired,
            "GUARD level_required must be 40. See: https://oldschool.runescape.wiki/w/Guard")
    }

    @Test fun `knight of ardougne level requirement is 55`() {
        assertEquals(55, config.pickpockets["KNIGHT_OF_ARDOUGNE"]!!.levelRequired,
            "KNIGHT_OF_ARDOUGNE level_required must be 55. See: https://oldschool.runescape.wiki/w/Knight_of_Ardougne")
    }

    @Test fun `paladin level requirement is 70`() {
        assertEquals(70, config.pickpockets["PALADIN"]!!.levelRequired,
            "PALADIN level_required must be 70. See: https://oldschool.runescape.wiki/w/Paladin")
    }

    @Test fun `hero level requirement is 80`() {
        assertEquals(80, config.pickpockets["HERO"]!!.levelRequired,
            "HERO level_required must be 80. See: https://oldschool.runescape.wiki/w/Hero")
    }

    // -------------------------------------------------------------------------
    // Pickpocket XP values
    // Source: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing
    // -------------------------------------------------------------------------

    @Test fun `man xp is 8 0`() {
        assertEquals(8.0, config.pickpockets["MAN"]!!.xp,
            "MAN xp must be 8.0. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    @Test fun `farmer xp is 14 5`() {
        assertEquals(14.5, config.pickpockets["FARMER"]!!.xp,
            "FARMER xp must be 14.5. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    @Test fun `master farmer xp is 43 0`() {
        assertEquals(43.0, config.pickpockets["MASTER_FARMER"]!!.xp,
            "MASTER_FARMER xp must be 43.0. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    @Test fun `guard xp is 46 8`() {
        assertEquals(46.8, config.pickpockets["GUARD"]!!.xp,
            "GUARD xp must be 46.8. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    @Test fun `knight of ardougne xp is 84 3`() {
        assertEquals(84.3, config.pickpockets["KNIGHT_OF_ARDOUGNE"]!!.xp,
            "KNIGHT_OF_ARDOUGNE xp must be 84.3. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    @Test fun `paladin xp is 151 75`() {
        assertEquals(151.75, config.pickpockets["PALADIN"]!!.xp,
            "PALADIN xp must be 151.75. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    @Test fun `hero xp is 275 0`() {
        assertEquals(275.0, config.pickpockets["HERO"]!!.xp,
            "HERO xp must be 275.0. See: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing")
    }

    // -------------------------------------------------------------------------
    // Success rate bounds
    // Source: https://oldschool.runescape.wiki/w/Thieving#Success_rate
    // -------------------------------------------------------------------------

    @Test fun `man success low is 168`() {
        assertEquals(168, config.pickpockets["MAN"]!!.successLow,
            "MAN success_low must be 168. See: https://oldschool.runescape.wiki/w/Thieving#Success_rate")
    }

    @Test fun `man success high is 240`() {
        assertEquals(240, config.pickpockets["MAN"]!!.successHigh,
            "MAN success_high must be 240. See: https://oldschool.runescape.wiki/w/Thieving#Success_rate")
    }

    @Test fun `man success rate at level 1 equals success low`() {
        val man = config.pickpockets["MAN"]!!
        assertEquals(man.successLow, man.successRate(1),
            "successRate at level_required must equal success_low. See: https://oldschool.runescape.wiki/w/Thieving#Success_rate")
    }

    @Test fun `man success rate at level 99 equals success high`() {
        val man = config.pickpockets["MAN"]!!
        assertEquals(man.successHigh, man.successRate(99),
            "successRate at level 99 must equal success_high. See: https://oldschool.runescape.wiki/w/Thieving#Success_rate")
    }

    @Test fun `master farmer success low is 110`() {
        assertEquals(110, config.pickpockets["MASTER_FARMER"]!!.successLow,
            "MASTER_FARMER success_low must be 110. See: https://oldschool.runescape.wiki/w/Thieving#Success_rate")
    }

    // -------------------------------------------------------------------------
    // Stun parameters
    // Source: https://oldschool.runescape.wiki/w/Stun
    // -------------------------------------------------------------------------

    @Test fun `man stun ticks is 4`() {
        assertEquals(4, config.pickpockets["MAN"]!!.stunTicks,
            "MAN stun_ticks must be 4. See: https://oldschool.runescape.wiki/w/Stun")
    }

    @Test fun `man stun damage is 1`() {
        assertEquals(1, config.pickpockets["MAN"]!!.stunDamage,
            "MAN stun_damage must be 1. See: https://oldschool.runescape.wiki/w/Man")
    }

    @Test fun `master farmer stun ticks is 5`() {
        assertEquals(5, config.pickpockets["MASTER_FARMER"]!!.stunTicks,
            "MASTER_FARMER stun_ticks must be 5. See: https://oldschool.runescape.wiki/w/Stun")
    }

    // -------------------------------------------------------------------------
    // Stall data
    // Source: https://oldschool.runescape.wiki/w/Thieving#Stalls
    // -------------------------------------------------------------------------

    @Test fun `four stalls loaded`() {
        assertEquals(4, config.stalls.size,
            "Expected 4 stalls. See: https://oldschool.runescape.wiki/w/Thieving#Stalls")
    }

    @Test fun `cake stall level requirement is 5`() {
        assertEquals(5, config.stalls["CAKE_STALL"]!!.levelRequired,
            "CAKE_STALL level_required must be 5. See: https://oldschool.runescape.wiki/w/Cake_stall")
    }

    @Test fun `cake stall xp is 16 0`() {
        assertEquals(16.0, config.stalls["CAKE_STALL"]!!.xp,
            "CAKE_STALL xp must be 16.0. See: https://oldschool.runescape.wiki/w/Cake_stall")
    }

    @Test fun `cake stall respawn ticks is 13`() {
        assertEquals(13, config.stalls["CAKE_STALL"]!!.respawnTicks,
            "CAKE_STALL respawn_ticks must be 13. See: https://oldschool.runescape.wiki/w/Cake_stall")
    }

    @Test fun `gem stall level requirement is 75`() {
        assertEquals(75, config.stalls["GEM_STALL"]!!.levelRequired,
            "GEM_STALL level_required must be 75. See: https://oldschool.runescape.wiki/w/Gem_stall")
    }

    @Test fun `gem stall xp is 160 0`() {
        assertEquals(160.0, config.stalls["GEM_STALL"]!!.xp,
            "GEM_STALL xp must be 160.0. See: https://oldschool.runescape.wiki/w/Gem_stall")
    }

    @Test fun `gem stall respawn ticks is 167`() {
        assertEquals(167, config.stalls["GEM_STALL"]!!.respawnTicks,
            "GEM_STALL respawn_ticks must be 167. See: https://oldschool.runescape.wiki/w/Gem_stall")
    }

    // -------------------------------------------------------------------------
    // Object/NPC IDs — all pending cache extraction
    // -------------------------------------------------------------------------

    @Test fun `all pickpocket npc id lists are empty pending cache extraction`() {
        val nonEmpty = config.pickpockets.values.filter { it.npcIds.isNotEmpty() }
        assertTrue(nonEmpty.isEmpty(),
            "All npc_ids should be empty pending /load-osrs-cache-full. " +
            "See: https://oldschool.runescape.wiki/w/Thieving")
    }

    @Test fun `all stall object ids are 0 pending cache extraction`() {
        val nonZero = config.stalls.values.filter { it.objectId != 0 }
        assertTrue(nonZero.isEmpty(),
            "All stall object_ids should be 0 pending /load-osrs-cache-full. " +
            "See: https://oldschool.runescape.wiki/w/Thieving#Stalls")
    }
}
