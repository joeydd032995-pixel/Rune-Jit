package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import skills.crafting.CraftingConfig
import skills.crafting.CraftingLoader
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CraftingParityTest {

    private lateinit var config: CraftingConfig

    @BeforeAll
    fun loadData() {
        config = CraftingLoader.load(Path.of("data/skills/crafting.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    @Test
    fun testTicksPerAttempt() {
        // 3 ticks per craft (1.8 seconds) — https://oldschool.runescape.wiki/w/Crafting
        assertEquals(3, config.meta.ticksPerAttempt,
            "Crafting ticks per attempt should be 3. See: https://oldschool.runescape.wiki/w/Crafting")
    }

    // -------------------------------------------------------------------------
    // Leather crafting — https://oldschool.runescape.wiki/w/Crafting#Leather
    // -------------------------------------------------------------------------

    @Test
    fun testLeatherGlovesXp() {
        val r = config.allRecipes.first { it.name == "LEATHER_GLOVES" }
        assertEquals(13.8, r.xp, 0.001,
            "Leather gloves XP should be 13.8. See: https://oldschool.runescape.wiki/w/Leather_gloves")
    }

    @Test
    fun testLeatherGlovesLevel() {
        val r = config.allRecipes.first { it.name == "LEATHER_GLOVES" }
        assertEquals(1, r.levelRequired,
            "Leather gloves level req should be 1. See: https://oldschool.runescape.wiki/w/Leather_gloves")
    }

    @Test
    fun testLeatherBootsXp() {
        val r = config.allRecipes.first { it.name == "LEATHER_BOOTS" }
        assertEquals(16.3, r.xp, 0.001,
            "Leather boots XP should be 16.3. See: https://oldschool.runescape.wiki/w/Leather_boots")
    }

    @Test
    fun testLeatherBodyXp() {
        val r = config.allRecipes.first { it.name == "LEATHER_BODY" }
        assertEquals(25.0, r.xp, 0.001,
            "Leather body XP should be 25.0. See: https://oldschool.runescape.wiki/w/Leather_body")
    }

    @Test
    fun testLeatherBodyLevel() {
        val r = config.allRecipes.first { it.name == "LEATHER_BODY" }
        assertEquals(14, r.levelRequired,
            "Leather body level req should be 14. See: https://oldschool.runescape.wiki/w/Leather_body")
    }

    @Test
    fun testHardleatherBodyXp() {
        val r = config.allRecipes.first { it.name == "HARDLEATHER_BODY" }
        assertEquals(35.0, r.xp, 0.001,
            "Hardleather body XP should be 35.0. See: https://oldschool.runescape.wiki/w/Hardleather_body")
    }

    @Test
    fun testHardleatherBodyLevel() {
        val r = config.allRecipes.first { it.name == "HARDLEATHER_BODY" }
        assertEquals(28, r.levelRequired,
            "Hardleather body level req should be 28. See: https://oldschool.runescape.wiki/w/Hardleather_body")
    }

    @Test
    fun testCoifXp() {
        val r = config.allRecipes.first { it.name == "COIF" }
        assertEquals(37.0, r.xp, 0.001,
            "Coif XP should be 37.0. See: https://oldschool.runescape.wiki/w/Coif")
    }

    // -------------------------------------------------------------------------
    // Gem cutting — https://oldschool.runescape.wiki/w/Crafting#Cutting_gems
    // -------------------------------------------------------------------------

    @Test
    fun testCutOpalXp() {
        val r = config.allRecipes.first { it.name == "CUT_OPAL" }
        assertEquals(15.0, r.xp, 0.001,
            "Cut opal XP should be 15.0. See: https://oldschool.runescape.wiki/w/Opal")
    }

    @Test
    fun testCutSapphireXp() {
        val r = config.allRecipes.first { it.name == "CUT_SAPPHIRE" }
        assertEquals(50.0, r.xp, 0.001,
            "Cut sapphire XP should be 50.0. See: https://oldschool.runescape.wiki/w/Sapphire")
    }

    @Test
    fun testCutSapphireLevel() {
        val r = config.allRecipes.first { it.name == "CUT_SAPPHIRE" }
        assertEquals(20, r.levelRequired,
            "Cut sapphire level req should be 20. See: https://oldschool.runescape.wiki/w/Sapphire")
    }

    @Test
    fun testCutEmeraldXp() {
        val r = config.allRecipes.first { it.name == "CUT_EMERALD" }
        assertEquals(67.5, r.xp, 0.001,
            "Cut emerald XP should be 67.5. See: https://oldschool.runescape.wiki/w/Emerald")
    }

    @Test
    fun testCutRubyXp() {
        val r = config.allRecipes.first { it.name == "CUT_RUBY" }
        assertEquals(85.0, r.xp, 0.001,
            "Cut ruby XP should be 85.0. See: https://oldschool.runescape.wiki/w/Ruby")
    }

    @Test
    fun testCutDiamondXp() {
        val r = config.allRecipes.first { it.name == "CUT_DIAMOND" }
        assertEquals(107.5, r.xp, 0.001,
            "Cut diamond XP should be 107.5. See: https://oldschool.runescape.wiki/w/Diamond")
    }

    @Test
    fun testCutDiamondLevel() {
        val r = config.allRecipes.first { it.name == "CUT_DIAMOND" }
        assertEquals(43, r.levelRequired,
            "Cut diamond level req should be 43. See: https://oldschool.runescape.wiki/w/Diamond")
    }

    // -------------------------------------------------------------------------
    // Jewellery — https://oldschool.runescape.wiki/w/Crafting#Jewellery
    // -------------------------------------------------------------------------

    @Test
    fun testGoldRingXp() {
        val r = config.allRecipes.first { it.name == "GOLD_RING" }
        assertEquals(15.0, r.xp, 0.001,
            "Gold ring XP should be 15.0. See: https://oldschool.runescape.wiki/w/Gold_ring")
    }

    @Test
    fun testGoldRingStation() {
        val r = config.allRecipes.first { it.name == "GOLD_RING" }
        assertEquals("FURNACE", r.station,
            "Gold ring requires FURNACE station. See: https://oldschool.runescape.wiki/w/Gold_ring")
    }

    @Test
    fun testSapphireRingXp() {
        val r = config.allRecipes.first { it.name == "SAPPHIRE_RING" }
        assertEquals(40.0, r.xp, 0.001,
            "Sapphire ring XP should be 40.0. See: https://oldschool.runescape.wiki/w/Sapphire_ring")
    }

    @Test
    fun testEmeraldRingXp() {
        val r = config.allRecipes.first { it.name == "EMERALD_RING" }
        assertEquals(55.0, r.xp, 0.001,
            "Emerald ring XP should be 55.0. See: https://oldschool.runescape.wiki/w/Emerald_ring")
    }

    @Test
    fun testDiamondRingXp() {
        val r = config.allRecipes.first { it.name == "DIAMOND_RING" }
        assertEquals(85.0, r.xp, 0.001,
            "Diamond ring XP should be 85.0. See: https://oldschool.runescape.wiki/w/Diamond_ring")
    }

    @Test
    fun testDiamondRingLevel() {
        val r = config.allRecipes.first { it.name == "DIAMOND_RING" }
        assertEquals(43, r.levelRequired,
            "Diamond ring level req should be 43. See: https://oldschool.runescape.wiki/w/Diamond_ring")
    }

    @Test
    fun testGoldNecklaceXp() {
        val r = config.allRecipes.first { it.name == "GOLD_NECKLACE" }
        assertEquals(20.0, r.xp, 0.001,
            "Gold necklace XP should be 20.0. See: https://oldschool.runescape.wiki/w/Gold_necklace")
    }

    @Test
    fun testGoldAmuletULevel() {
        val r = config.allRecipes.first { it.name == "GOLD_AMULET_U" }
        assertEquals(8, r.levelRequired,
            "Gold amulet (u) level req should be 8. See: https://oldschool.runescape.wiki/w/Gold_amulet_(u)")
    }

    // -------------------------------------------------------------------------
    // Glassblowing — https://oldschool.runescape.wiki/w/Crafting#Glassblowing
    // -------------------------------------------------------------------------

    @Test
    fun testBeerGlassXp() {
        val r = config.allRecipes.first { it.name == "BEER_GLASS" }
        assertEquals(17.5, r.xp, 0.001,
            "Beer glass XP should be 17.5. See: https://oldschool.runescape.wiki/w/Beer_glass")
    }

    @Test
    fun testVialXp() {
        val r = config.allRecipes.first { it.name == "VIAL" }
        assertEquals(35.0, r.xp, 0.001,
            "Vial XP should be 35.0. See: https://oldschool.runescape.wiki/w/Vial")
    }

    @Test
    fun testUnpoweredOrbLevel() {
        val r = config.allRecipes.first { it.name == "UNPOWERED_ORB" }
        assertEquals(46, r.levelRequired,
            "Unpowered orb level req should be 46. See: https://oldschool.runescape.wiki/w/Unpowered_orb")
    }

    @Test
    fun testUnpoweredOrbXp() {
        val r = config.allRecipes.first { it.name == "UNPOWERED_ORB" }
        assertEquals(52.5, r.xp, 0.001,
            "Unpowered orb XP should be 52.5. See: https://oldschool.runescape.wiki/w/Unpowered_orb")
    }

    // -------------------------------------------------------------------------
    // Spinning — https://oldschool.runescape.wiki/w/Crafting#Spinning
    // -------------------------------------------------------------------------

    @Test
    fun testBallOfWoolXp() {
        val r = config.allRecipes.first { it.name == "BALL_OF_WOOL" }
        assertEquals(2.5, r.xp, 0.001,
            "Ball of wool XP should be 2.5. See: https://oldschool.runescape.wiki/w/Ball_of_wool")
    }

    @Test
    fun testBallOfWoolStation() {
        val r = config.allRecipes.first { it.name == "BALL_OF_WOOL" }
        assertEquals("SPINNING_WHEEL", r.station,
            "Ball of wool requires SPINNING_WHEEL. See: https://oldschool.runescape.wiki/w/Ball_of_wool")
    }

    @Test
    fun testBowStringXp() {
        val r = config.allRecipes.first { it.name == "BOW_STRING" }
        assertEquals(15.0, r.xp, 0.001,
            "Bow string XP should be 15.0. See: https://oldschool.runescape.wiki/w/Bow_string")
    }

    @Test
    fun testBowStringLevel() {
        val r = config.allRecipes.first { it.name == "BOW_STRING" }
        assertEquals(10, r.levelRequired,
            "Bow string level req should be 10. See: https://oldschool.runescape.wiki/w/Bow_string")
    }

    // -------------------------------------------------------------------------
    // Index correctness
    // -------------------------------------------------------------------------

    @Test
    fun testByInputItemIdIndexSapphire() {
        // Uncut sapphire item ID 1623 → CUT_SAPPHIRE
        // Source: https://oldschool.runescape.wiki/w/Uncut_sapphire
        val recipes = config.byInputItemId[1623]
        assertNotNull(recipes, "byInputItemId should have entry for uncut sapphire (1623)")
        assertEquals("CUT_SAPPHIRE", recipes!!.first { it.category == "gems" }.name,
            "Recipe for uncut sapphire (1623) should be CUT_SAPPHIRE. See: https://oldschool.runescape.wiki/w/Sapphire")
    }

    @Test
    fun testByInputItemIdIndexLeather() {
        // Leather item ID 1741 → leather recipes
        // Source: https://oldschool.runescape.wiki/w/Leather
        val recipes = config.byInputItemId[1741]
        assertNotNull(recipes, "byInputItemId should have entries for leather (1741)")
        assertEquals(true, recipes!!.any { it.category == "leather" },
            "byInputItemId[1741] should include leather crafting recipes. See: https://oldschool.runescape.wiki/w/Leather")
    }

    @Test
    fun testStationRecipesFurnacePresent() {
        val furnaceRecipes = config.stationRecipes["FURNACE"]
        assertNotNull(furnaceRecipes,
            "stationRecipes should contain FURNACE entries. See: https://oldschool.runescape.wiki/w/Crafting#Jewellery")
        assertEquals(true, furnaceRecipes!!.isNotEmpty(),
            "FURNACE recipe list should not be empty. See: https://oldschool.runescape.wiki/w/Crafting#Jewellery")
    }

    @Test
    fun testStationRecipesSpinningWheelPresent() {
        val spinRecipes = config.stationRecipes["SPINNING_WHEEL"]
        assertNotNull(spinRecipes,
            "stationRecipes should contain SPINNING_WHEEL entries. See: https://oldschool.runescape.wiki/w/Crafting#Spinning")
        assertEquals(true, spinRecipes!!.isNotEmpty(),
            "SPINNING_WHEEL recipe list should not be empty. See: https://oldschool.runescape.wiki/w/Crafting#Spinning")
    }
}
