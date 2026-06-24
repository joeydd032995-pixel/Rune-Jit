package parity.skills

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import skills.fishing.FishingConfig
import skills.fishing.FishingLoader
import java.nio.file.Path

/**
 * Fishing XP values and mechanic parity tests.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Fishing
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FishingXpTest {

    private lateinit var config: FishingConfig

    @BeforeAll
    fun loadData() {
        config = FishingLoader.load(Path.of("data/skills/fishing.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * 5 ticks per fishing attempt (3.0 seconds).
     * Source: https://oldschool.runescape.wiki/w/Fishing#Mechanics
     */
    @Test
    fun `ticks per attempt is 5`() {
        assertEquals(5, config.meta.ticksPerAttempt,
            "Fishing ticks_per_attempt must be 5. See: https://oldschool.runescape.wiki/w/Fishing#Mechanics")
    }

    // -------------------------------------------------------------------------
    // Spot XP values — Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
    // -------------------------------------------------------------------------

    /**
     * Shrimps XP is 10.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `shrimp xp is 10_0`() {
        assertEquals(10.0, config.spots["SHRIMP"]!!.xp,
            "Shrimp XP should be 10.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Sardine XP is 20.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `sardine xp is 20_0`() {
        assertEquals(20.0, config.spots["SARDINE"]!!.xp,
            "Sardine XP should be 20.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Herring XP is 30.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `herring xp is 30_0`() {
        assertEquals(30.0, config.spots["HERRING"]!!.xp,
            "Herring XP should be 30.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Trout XP is 50.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `trout xp is 50_0`() {
        assertEquals(50.0, config.spots["TROUT"]!!.xp,
            "Trout XP should be 50.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Salmon XP is 70.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `salmon xp is 70_0`() {
        assertEquals(70.0, config.spots["SALMON"]!!.xp,
            "Salmon XP should be 70.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Lobster XP is 90.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `lobster xp is 90_0`() {
        assertEquals(90.0, config.spots["LOBSTER"]!!.xp,
            "Lobster XP should be 90.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Shark XP is 110.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `shark xp is 110_0`() {
        assertEquals(110.0, config.spots["SHARK"]!!.xp,
            "Shark XP should be 110.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Anglerfish XP is 120.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `anglerfish xp is 120_0`() {
        assertEquals(120.0, config.spots["ANGLERFISH"]!!.xp,
            "Anglerfish XP should be 120.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    /**
     * Dark crab XP is 130.0.
     * Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table
     */
    @Test
    fun `dark crab xp is 130_0`() {
        assertEquals(130.0, config.spots["DARK_CRAB"]!!.xp,
            "Dark Crab XP should be 130.0. See: https://oldschool.runescape.wiki/w/Fishing#Experience_table")
    }

    // -------------------------------------------------------------------------
    // Level requirements — Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_spots
    // -------------------------------------------------------------------------

    @Test fun `shrimp requires level 1`() = assertSpotLevel("SHRIMP", 1)
    @Test fun `sardine requires level 5`() = assertSpotLevel("SARDINE", 5)
    @Test fun `herring requires level 10`() = assertSpotLevel("HERRING", 10)
    @Test fun `anchovies requires level 15`() = assertSpotLevel("ANCHOVIES", 15)
    @Test fun `mackerel requires level 16`() = assertSpotLevel("MACKEREL", 16)
    @Test fun `trout requires level 20`() = assertSpotLevel("TROUT", 20)
    @Test fun `cod requires level 23`() = assertSpotLevel("COD", 23)
    @Test fun `pike requires level 25`() = assertSpotLevel("PIKE", 25)
    @Test fun `salmon requires level 30`() = assertSpotLevel("SALMON", 30)
    @Test fun `tuna requires level 35`() = assertSpotLevel("TUNA", 35)
    @Test fun `lobster requires level 40`() = assertSpotLevel("LOBSTER", 40)
    @Test fun `bass requires level 46`() = assertSpotLevel("BASS", 46)
    @Test fun `swordfish requires level 50`() = assertSpotLevel("SWORDFISH", 50)

    /**
     * Monkfish requires level 62.
     * Source: https://oldschool.runescape.wiki/w/Raw_monkfish
     */
    @Test
    fun `monkfish requires level 62`() {
        assertEquals(62, config.spots["MONKFISH"]!!.levelRequired,
            "Monkfish level_required should be 62. See: https://oldschool.runescape.wiki/w/Raw_monkfish")
    }

    @Test fun `shark requires level 76`() = assertSpotLevel("SHARK", 76)
    @Test fun `anglerfish requires level 82`() = assertSpotLevel("ANGLERFISH", 82)
    @Test fun `dark crab requires level 85`() = assertSpotLevel("DARK_CRAB", 85)

    // -------------------------------------------------------------------------
    // Bait requirements — Source: https://oldschool.runescape.wiki/w/Fishing#Bait
    // -------------------------------------------------------------------------

    /**
     * Sardine requires fishing bait (item ID 313).
     * Source: https://oldschool.runescape.wiki/w/Fishing_bait
     */
    @Test
    fun `sardine requires bait item id 313`() {
        assertEquals(313, config.spots["SARDINE"]!!.baitItemId,
            "Sardine bait_item_id should be 313 (Fishing bait). See: https://oldschool.runescape.wiki/w/Fishing_bait")
    }

    /**
     * Trout requires feather bait (item ID 314).
     * Source: https://oldschool.runescape.wiki/w/Feather
     */
    @Test
    fun `trout requires feather bait item id 314`() {
        assertEquals(314, config.spots["TROUT"]!!.baitItemId,
            "Trout bait_item_id should be 314 (Feather). See: https://oldschool.runescape.wiki/w/Feather")
    }

    /**
     * Shrimp requires no bait (bait_item_id = 0).
     * Source: https://oldschool.runescape.wiki/w/Fishing#Small_net_fishing
     */
    @Test
    fun `shrimp requires no bait`() {
        assertEquals(0, config.spots["SHRIMP"]!!.baitItemId,
            "Shrimp bait_item_id should be 0 (no bait). See: https://oldschool.runescape.wiki/w/Fishing#Small_net_fishing")
    }

    // -------------------------------------------------------------------------
    // Tool IDs — Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_equipment
    // -------------------------------------------------------------------------

    /**
     * Shrimp uses small fishing net (item ID 303).
     * Source: https://oldschool.runescape.wiki/w/Small_fishing_net
     */
    @Test
    fun `shrimp uses small net item id 303`() {
        assertEquals(303, config.spots["SHRIMP"]!!.toolItemId,
            "Shrimp tool_item_id should be 303 (Small fishing net). See: https://oldschool.runescape.wiki/w/Small_fishing_net")
    }

    /**
     * Trout uses fly fishing rod (item ID 309).
     * Source: https://oldschool.runescape.wiki/w/Fly_fishing_rod
     */
    @Test
    fun `trout uses fly rod item id 309`() {
        assertEquals(309, config.spots["TROUT"]!!.toolItemId,
            "Trout tool_item_id should be 309 (Fly fishing rod). See: https://oldschool.runescape.wiki/w/Fly_fishing_rod")
    }

    // -------------------------------------------------------------------------
    // Fish item IDs — Source: https://oldschool.runescape.wiki/w/Fishing
    // -------------------------------------------------------------------------

    @Test fun `shrimp fish item id is 317`() = assertFishItemId("SHRIMP", 317)
    @Test fun `lobster fish item id is 377`() = assertFishItemId("LOBSTER", 377)
    @Test fun `shark fish item id is 383`() = assertFishItemId("SHARK", 383)
    @Test fun `monkfish fish item id is 7944`() = assertFishItemId("MONKFISH", 7944)
    @Test fun `anglerfish fish item id is 13439`() = assertFishItemId("ANGLERFISH", 13439)
    @Test fun `dark crab fish item id is 11934`() = assertFishItemId("DARK_CRAB", 11934)

    // -------------------------------------------------------------------------
    // Heron pet — Source: https://oldschool.runescape.wiki/w/Heron#Drop_rate
    // -------------------------------------------------------------------------

    /**
     * Heron pet item ID is 13320.
     * Source: https://oldschool.runescape.wiki/w/Heron
     */
    @Test
    fun `heron pet item id is 13320`() {
        assertEquals(13320, config.pet.itemId,
            "Heron pet item_id should be 13320. See: https://oldschool.runescape.wiki/w/Heron")
    }

    /**
     * Heron base rate is 257803.
     * Source: https://oldschool.runescape.wiki/w/Heron#Drop_rate
     */
    @Test
    fun `heron pet base rate is 257803`() {
        assertEquals(257803, config.pet.baseRate,
            "Heron pet base_rate should be 257803. See: https://oldschool.runescape.wiki/w/Heron#Drop_rate")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertSpotLevel(spotName: String, expectedLevel: Int) {
        val spot = config.spots[spotName]
            ?: error("Spot '$spotName' not found in fishing.yaml")
        assertEquals(expectedLevel, spot.levelRequired,
            "$spotName level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Fishing#Fishing_spots")
    }

    private fun assertFishItemId(spotName: String, expectedItemId: Int) {
        val spot = config.spots[spotName]
            ?: error("Spot '$spotName' not found in fishing.yaml")
        assertEquals(expectedItemId, spot.fishItemId,
            "$spotName fish_item_id should be $expectedItemId. See: https://oldschool.runescape.wiki/w/Fishing")
    }
}
