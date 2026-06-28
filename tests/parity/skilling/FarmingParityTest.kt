package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import skills.farming.FarmingConfig
import skills.farming.FarmingLoader
import java.nio.file.Path

/**
 * Parity tests for Farming skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Farming
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FarmingParityTest {

    private lateinit var config: FarmingConfig

    @BeforeAll
    fun loadData() {
        config = FarmingLoader.load(Path.of("data/skills/farming.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * Default growth ticks: 500 (~5 minutes per stage at 0.6s/tick).
     * Source: https://oldschool.runescape.wiki/w/Farming#Growth
     */
    @Test fun `default growth ticks is 500`() {
        assertEquals(500, config.meta.defaultGrowthTicks,
            "default_growth_ticks must be 500. See: https://oldschool.runescape.wiki/w/Farming#Growth")
    }

    /**
     * At least 14 patches defined (7 allotment + 7 herb minimum).
     * Source: https://oldschool.runescape.wiki/w/Farming#Patches
     */
    @Test fun `at least 14 patches are defined`() {
        assertTrue(config.patches.size >= 14,
            "Must define at least 14 patches (7 allotment + 7 herb). See: https://oldschool.runescape.wiki/w/Farming#Patches")
    }

    // -------------------------------------------------------------------------
    // Allotment patches — Source: https://oldschool.runescape.wiki/w/Allotment_patch
    // -------------------------------------------------------------------------

    /**
     * Potato: level 1, plant XP 8.0, harvest XP 9.0, seed ID 5318.
     * Source: https://oldschool.runescape.wiki/w/Potato_seed
     */
    @Test fun `potato level required is 1`() = assertPatchLevel("POTATO", 1,
        "https://oldschool.runescape.wiki/w/Potato_seed")

    @Test fun `potato plant xp is 8_0`() = assertPatchPlantXp("POTATO", 8.0,
        "https://oldschool.runescape.wiki/w/Potato_seed")

    @Test fun `potato harvest xp is 9_0`() = assertPatchHarvestXp("POTATO", 9.0,
        "https://oldschool.runescape.wiki/w/Potato_seed")

    @Test fun `potato seed item id is 5318`() {
        val patch = config.patches["POTATO"]!!
        assertEquals(5318, patch.seedItemId,
            "Potato seed_item_id must be 5318. See: https://oldschool.runescape.wiki/w/Potato_seed")
    }

    @Test fun `potato growth ticks is 500`() {
        val patch = config.patches["POTATO"]!!
        assertEquals(500, patch.growthTicks,
            "Potato growth_ticks must be 500. See: https://oldschool.runescape.wiki/w/Farming#Growth")
    }

    /**
     * Onion: level 5, plant XP 10.5.
     * Source: https://oldschool.runescape.wiki/w/Onion_seed
     */
    @Test fun `onion level required is 5`() = assertPatchLevel("ONION", 5,
        "https://oldschool.runescape.wiki/w/Onion_seed")

    @Test fun `onion plant xp is 10_5`() = assertPatchPlantXp("ONION", 10.5,
        "https://oldschool.runescape.wiki/w/Onion_seed")

    /**
     * Watermelon: level 47, plant XP 48.5.
     * Source: https://oldschool.runescape.wiki/w/Watermelon_seed
     */
    @Test fun `watermelon level required is 47`() = assertPatchLevel("WATERMELON", 47,
        "https://oldschool.runescape.wiki/w/Watermelon_seed")

    @Test fun `watermelon plant xp is 48_5`() = assertPatchPlantXp("WATERMELON", 48.5,
        "https://oldschool.runescape.wiki/w/Watermelon_seed")

    // -------------------------------------------------------------------------
    // Herb patches — Source: https://oldschool.runescape.wiki/w/Herb_patch
    // -------------------------------------------------------------------------

    /**
     * Guam: level 9, patch type HERB, plant XP 11.0.
     * Source: https://oldschool.runescape.wiki/w/Guam_seed
     */
    @Test fun `guam level required is 9`() = assertPatchLevel("GUAM", 9,
        "https://oldschool.runescape.wiki/w/Guam_seed")

    @Test fun `guam patch type is HERB`() {
        val patch = config.patches["GUAM"]!!
        assertEquals("HERB", patch.patchType,
            "Guam patch_type must be HERB. See: https://oldschool.runescape.wiki/w/Herb_patch")
    }

    @Test fun `guam plant xp is 11_0`() = assertPatchPlantXp("GUAM", 11.0,
        "https://oldschool.runescape.wiki/w/Guam_seed")

    /**
     * Ranarr: level 32, plant XP 27.0.
     * Source: https://oldschool.runescape.wiki/w/Ranarr_seed
     */
    @Test fun `ranarr level required is 32`() = assertPatchLevel("RANARR", 32,
        "https://oldschool.runescape.wiki/w/Ranarr_seed")

    @Test fun `ranarr plant xp is 27_0`() = assertPatchPlantXp("RANARR", 27.0,
        "https://oldschool.runescape.wiki/w/Ranarr_seed")

    /**
     * Torstol: level 85, plant XP 199.5, harvest XP 224.5.
     * Source: https://oldschool.runescape.wiki/w/Torstol_seed
     */
    @Test fun `torstol level required is 85`() = assertPatchLevel("TORSTOL", 85,
        "https://oldschool.runescape.wiki/w/Torstol_seed")

    @Test fun `torstol plant xp is 199_5`() = assertPatchPlantXp("TORSTOL", 199.5,
        "https://oldschool.runescape.wiki/w/Torstol_seed")

    @Test fun `torstol harvest xp is 224_5`() = assertPatchHarvestXp("TORSTOL", 224.5,
        "https://oldschool.runescape.wiki/w/Torstol_seed")

    /**
     * Snapdragon: level 62.
     * Source: https://oldschool.runescape.wiki/w/Snapdragon_seed
     */
    @Test fun `snapdragon level required is 62`() = assertPatchLevel("SNAPDRAGON", 62,
        "https://oldschool.runescape.wiki/w/Snapdragon_seed")

    // -------------------------------------------------------------------------
    // Patch object IDs pending cache extraction
    // -------------------------------------------------------------------------

    /**
     * All patch object IDs must be 0 until cache extraction is complete.
     * Source: tasks/farming — pending /cache-unpack-extract-assets
     */
    @Test fun `all patch object ids are 0 pending cache extraction`() {
        for ((name, patch) in config.patches) {
            assertEquals(0, patch.patchObjectId,
                "$name patch_object_id must be 0 (pending cache extraction). " +
                "Run /cache-unpack-extract-assets to populate. " +
                "See: https://oldschool.runescape.wiki/w/Farming#Patches")
        }
    }

    // -------------------------------------------------------------------------
    // Lookup maps
    // -------------------------------------------------------------------------

    /**
     * bySeedItemId must map potato seed (5318) to POTATO patch.
     * Source: https://oldschool.runescape.wiki/w/Potato_seed
     */
    @Test fun `potato seed is in bySeedItemId lookup`() {
        val patch = config.bySeedItemId[5318]
        assertEquals("POTATO", patch?.name,
            "bySeedItemId[5318] must map to POTATO. See: https://oldschool.runescape.wiki/w/Potato_seed")
    }

    /**
     * byPatchObjectId must be empty because all object IDs are 0.
     * Source: tasks/farming — pending cache extraction.
     */
    @Test fun `byPatchObjectId is empty when all objectIds are 0`() {
        assertTrue(config.byPatchObjectId.isEmpty(),
            "byPatchObjectId must be empty when all patch_object_ids are 0. " +
            "See: https://oldschool.runescape.wiki/w/Farming#Patches")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertPatchLevel(patchName: String, expectedLevel: Int, source: String) {
        val patch = config.patches[patchName]
            ?: error("Patch '$patchName' not found in farming.yaml")
        assertEquals(expectedLevel, patch.levelRequired,
            "$patchName level_required should be $expectedLevel. See: $source")
    }

    private fun assertPatchPlantXp(patchName: String, expectedXp: Double, source: String) {
        val patch = config.patches[patchName]
            ?: error("Patch '$patchName' not found in farming.yaml")
        assertEquals(expectedXp, patch.plantXp,
            "$patchName plant_xp should be $expectedXp. See: $source")
    }

    private fun assertPatchHarvestXp(patchName: String, expectedXp: Double, source: String) {
        val patch = config.patches[patchName]
            ?: error("Patch '$patchName' not found in farming.yaml")
        assertEquals(expectedXp, patch.harvestXp,
            "$patchName harvest_xp should be $expectedXp. See: $source")
    }
}
