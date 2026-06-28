package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import skills.smithing.SmithingConfig
import skills.smithing.SmithingLoader
import java.nio.file.Path

/**
 * Parity tests for Smithing skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Smithing
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SmithingParityTest {

    private lateinit var config: SmithingConfig

    @BeforeAll
    fun loadData() {
        config = SmithingLoader.load(Path.of("data/skills/smithing.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * 4 ticks per smithing/smelting attempt (2.4 seconds).
     * Source: https://oldschool.runescape.wiki/w/Smithing#Mechanics
     */
    @Test fun `tick rate is 4 ticks per attempt`() {
        assertEquals(4, config.meta.ticksPerAttempt,
            "Smithing ticks_per_attempt must be 4. See: https://oldschool.runescape.wiki/w/Smithing#Mechanics")
    }

    /**
     * Hammer item ID is 2347.
     * Source: https://oldschool.runescape.wiki/w/Hammer
     */
    @Test fun `hammer item id is 2347`() {
        assertEquals(2347, config.meta.hammerItemId,
            "hammer_item_id must be 2347. See: https://oldschool.runescape.wiki/w/Hammer")
    }

    // -------------------------------------------------------------------------
    // Bar smelt XP — Source: https://oldschool.runescape.wiki/w/Smithing#Smelting
    // -------------------------------------------------------------------------

    @Test fun `bronze bar smelt xp is 6_2`() = assertBarXp("BRONZE", 6.2,
        "https://oldschool.runescape.wiki/w/Bronze_bar")

    @Test fun `iron bar smelt xp is 12_5`() = assertBarXp("IRON", 12.5,
        "https://oldschool.runescape.wiki/w/Iron_bar")

    @Test fun `silver bar smelt xp is 13_7`() = assertBarXp("SILVER", 13.7,
        "https://oldschool.runescape.wiki/w/Silver_bar")

    @Test fun `steel bar smelt xp is 17_5`() = assertBarXp("STEEL", 17.5,
        "https://oldschool.runescape.wiki/w/Steel_bar")

    @Test fun `gold bar smelt xp is 22_5`() = assertBarXp("GOLD", 22.5,
        "https://oldschool.runescape.wiki/w/Gold_bar")

    @Test fun `mithril bar smelt xp is 30_0`() = assertBarXp("MITHRIL", 30.0,
        "https://oldschool.runescape.wiki/w/Mithril_bar")

    @Test fun `adamant bar smelt xp is 37_5`() = assertBarXp("ADAMANT", 37.5,
        "https://oldschool.runescape.wiki/w/Adamantite_bar")

    @Test fun `rune bar smelt xp is 50_0`() = assertBarXp("RUNE", 50.0,
        "https://oldschool.runescape.wiki/w/Runite_bar")

    // -------------------------------------------------------------------------
    // Bar level requirements — Source: https://oldschool.runescape.wiki/w/Smithing#Smelting
    // -------------------------------------------------------------------------

    @Test fun `bronze bar requires level 1`() = assertBarLevel("BRONZE", 1)
    @Test fun `iron bar requires level 15`() = assertBarLevel("IRON", 15)
    @Test fun `silver bar requires level 20`() = assertBarLevel("SILVER", 20)
    @Test fun `steel bar requires level 30`() = assertBarLevel("STEEL", 30)
    @Test fun `gold bar requires level 40`() = assertBarLevel("GOLD", 40)
    @Test fun `mithril bar requires level 50`() = assertBarLevel("MITHRIL", 50)
    @Test fun `adamant bar requires level 70`() = assertBarLevel("ADAMANT", 70)
    @Test fun `rune bar requires level 85`() = assertBarLevel("RUNE", 85)

    // -------------------------------------------------------------------------
    // Bar ingredients (coal counts) — Source: https://oldschool.runescape.wiki/w/Smithing#Smelting
    // -------------------------------------------------------------------------

    @Test fun `bronze bar uses copper and tin`() {
        val bar = config.bars["BRONZE"]!!
        val oreIds = bar.ingredients.map { it.oreItemId }.toSet()
        assertEquals(setOf(436, 438), oreIds,
            "Bronze bar ingredients must be copper ore (436) and tin ore (438). See: https://oldschool.runescape.wiki/w/Bronze_bar")
    }

    @Test fun `steel bar requires 2 coal`() {
        val bar = config.bars["STEEL"]!!
        val coal = bar.ingredients.first { it.oreItemId == 453 }
        assertEquals(2, coal.qty,
            "Steel bar requires 2 coal. See: https://oldschool.runescape.wiki/w/Steel_bar")
    }

    @Test fun `mithril bar requires 4 coal`() {
        val bar = config.bars["MITHRIL"]!!
        val coal = bar.ingredients.first { it.oreItemId == 453 }
        assertEquals(4, coal.qty,
            "Mithril bar requires 4 coal. See: https://oldschool.runescape.wiki/w/Mithril_bar")
    }

    @Test fun `adamant bar requires 6 coal`() {
        val bar = config.bars["ADAMANT"]!!
        val coal = bar.ingredients.first { it.oreItemId == 453 }
        assertEquals(6, coal.qty,
            "Adamantite bar requires 6 coal. See: https://oldschool.runescape.wiki/w/Adamantite_bar")
    }

    @Test fun `rune bar requires 8 coal`() {
        val bar = config.bars["RUNE"]!!
        val coal = bar.ingredients.first { it.oreItemId == 453 }
        assertEquals(8, coal.qty,
            "Runite bar requires 8 coal. See: https://oldschool.runescape.wiki/w/Runite_bar")
    }

    // -------------------------------------------------------------------------
    // Smithing item XP — Source: https://oldschool.runescape.wiki/w/Smithing#Experience
    // XP formula: bars_required × xp_per_bar_for_metal
    // -------------------------------------------------------------------------

    @Test fun `bronze sword smith xp is 12_5`() = assertSmithXp("BRONZE_SWORD", 12.5,
        "https://oldschool.runescape.wiki/w/Bronze_sword")

    @Test fun `bronze platebody smith xp is 62_5`() = assertSmithXp("BRONZE_PLATEBODY", 62.5,
        "https://oldschool.runescape.wiki/w/Bronze_platebody")

    @Test fun `iron platebody smith xp is 125_0`() = assertSmithXp("IRON_PLATEBODY", 125.0,
        "https://oldschool.runescape.wiki/w/Iron_platebody")

    @Test fun `steel platebody smith xp is 187_5`() = assertSmithXp("STEEL_PLATEBODY", 187.5,
        "https://oldschool.runescape.wiki/w/Steel_platebody")

    @Test fun `mithril platebody smith xp is 250_0`() = assertSmithXp("MITHRIL_PLATEBODY", 250.0,
        "https://oldschool.runescape.wiki/w/Mithril_platebody")

    @Test fun `adamant platebody smith xp is 312_5`() = assertSmithXp("ADAMANT_PLATEBODY", 312.5,
        "https://oldschool.runescape.wiki/w/Adamant_platebody")

    @Test fun `rune platebody smith xp is 375_0`() = assertSmithXp("RUNE_PLATEBODY", 375.0,
        "https://oldschool.runescape.wiki/w/Rune_platebody")

    @Test fun `rune scimitar smith xp is 150_0`() = assertSmithXp("RUNE_SCIMITAR", 150.0,
        "https://oldschool.runescape.wiki/w/Rune_scimitar")

    // -------------------------------------------------------------------------
    // Smithing item level requirements — Source: https://oldschool.runescape.wiki/w/Smithing#Smithing_2
    // -------------------------------------------------------------------------

    @Test fun `bronze sword requires level 4`() = assertSmithLevel("BRONZE_SWORD", 4)
    @Test fun `bronze scimitar requires level 5`() = assertSmithLevel("BRONZE_SCIMITAR", 5)
    @Test fun `bronze platelegs requires level 16`() = assertSmithLevel("BRONZE_PLATELEGS", 16)
    @Test fun `bronze platebody requires level 18`() = assertSmithLevel("BRONZE_PLATEBODY", 18)
    @Test fun `iron sword requires level 19`() = assertSmithLevel("IRON_SWORD", 19)
    @Test fun `iron scimitar requires level 20`() = assertSmithLevel("IRON_SCIMITAR", 20)
    @Test fun `iron platebody requires level 33`() = assertSmithLevel("IRON_PLATEBODY", 33)
    @Test fun `steel sword requires level 34`() = assertSmithLevel("STEEL_SWORD", 34)
    @Test fun `mithril scimitar requires level 55`() = assertSmithLevel("MITHRIL_SCIMITAR", 55)
    @Test fun `adamant scimitar requires level 75`() = assertSmithLevel("ADAMANT_SCIMITAR", 75)
    @Test fun `rune scimitar requires level 90`() = assertSmithLevel("RUNE_SCIMITAR", 90)
    @Test fun `rune platebody requires level 99`() = assertSmithLevel("RUNE_PLATEBODY", 99)

    // -------------------------------------------------------------------------
    // Bar item IDs — Source: https://oldschool.runescape.wiki/w/Smithing#Bars
    // -------------------------------------------------------------------------

    @Test fun `bronze bar item id is 2349`() {
        assertEquals(2349, config.bars["BRONZE"]!!.barItemId,
            "Bronze bar item ID must be 2349. See: https://oldschool.runescape.wiki/w/Bronze_bar")
    }

    @Test fun `rune bar item id is 2363`() {
        assertEquals(2363, config.bars["RUNE"]!!.barItemId,
            "Runite bar item ID must be 2363. See: https://oldschool.runescape.wiki/w/Runite_bar")
    }

    // -------------------------------------------------------------------------
    // Derived lookups
    // -------------------------------------------------------------------------

    @Test fun `barByItemId resolves bronze bar`() {
        val bar = config.barByItemId[2349]
        assertEquals("BRONZE", bar?.name,
            "Bronze bar (item 2349) must resolve to BRONZE via barByItemId. See: https://oldschool.runescape.wiki/w/Bronze_bar")
    }

    @Test fun `smithByBarType groups rune items`() {
        val runeItems = config.smithByBarType["RUNE"]
        val names = runeItems?.map { it.name }?.toSet() ?: emptySet()
        assert("RUNE_PLATEBODY" in names) {
            "smithByBarType[RUNE] must include RUNE_PLATEBODY. See: https://oldschool.runescape.wiki/w/Smithing#Smithing_2"
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertBarXp(barName: String, expectedXp: Double, source: String) {
        val bar = config.bars[barName]
            ?: error("Bar '$barName' not found in smithing.yaml")
        assertEquals(expectedXp, bar.smeltXp,
            "$barName smelt_xp should be $expectedXp. See: $source")
    }

    private fun assertBarLevel(barName: String, expectedLevel: Int) {
        val bar = config.bars[barName]
            ?: error("Bar '$barName' not found in smithing.yaml")
        assertEquals(expectedLevel, bar.levelRequired,
            "$barName level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Smithing#Smelting")
    }

    private fun assertSmithXp(itemName: String, expectedXp: Double, source: String) {
        val item = config.smithing[itemName]
            ?: error("Smith item '$itemName' not found in smithing.yaml")
        assertEquals(expectedXp, item.smithXp,
            "$itemName smith_xp should be $expectedXp. See: $source")
    }

    private fun assertSmithLevel(itemName: String, expectedLevel: Int) {
        val item = config.smithing[itemName]
            ?: error("Smith item '$itemName' not found in smithing.yaml")
        assertEquals(expectedLevel, item.levelRequired,
            "$itemName level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Smithing#Smithing_2")
    }
}
