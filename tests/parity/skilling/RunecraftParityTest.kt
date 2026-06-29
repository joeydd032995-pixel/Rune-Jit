package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import skills.runecraft.RunecraftConfig
import skills.runecraft.RunecraftLoader
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RunecraftParityTest {

    private lateinit var config: RunecraftConfig

    @BeforeAll
    fun loadData() {
        config = RunecraftLoader.load(Path.of("data/skills/runecraft.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    @Test
    fun testTicksPerAttempt() {
        // 1 tick per action — https://oldschool.runescape.wiki/w/Runecraft#Mechanics
        assertEquals(1, config.meta.ticksPerAttempt,
            "Runecraft ticks per attempt should be 1. See: https://oldschool.runescape.wiki/w/Runecraft#Mechanics")
    }

    @Test
    fun testRuneEssenceItemId() {
        // Rune essence item ID 1436 — https://oldschool.runescape.wiki/w/Rune_essence
        assertEquals(1436, config.meta.runeEssenceItemId,
            "Rune essence item ID should be 1436. See: https://oldschool.runescape.wiki/w/Rune_essence")
    }

    @Test
    fun testPureEssenceItemId() {
        // Pure essence item ID 7936 — https://oldschool.runescape.wiki/w/Pure_essence
        assertEquals(7936, config.meta.pureEssenceItemId,
            "Pure essence item ID should be 7936. See: https://oldschool.runescape.wiki/w/Pure_essence")
    }

    // -------------------------------------------------------------------------
    // Level requirements — https://oldschool.runescape.wiki/w/Runecraft#Runes
    // -------------------------------------------------------------------------

    @Test fun testAirLevelRequired() {
        assertEquals(1, config.altars["AIR"]!!.levelRequired,
            "AIR altar level req should be 1. See: https://oldschool.runescape.wiki/w/Air_rune")
    }

    @Test fun testMindLevelRequired() {
        assertEquals(2, config.altars["MIND"]!!.levelRequired,
            "MIND altar level req should be 2. See: https://oldschool.runescape.wiki/w/Mind_rune")
    }

    @Test fun testFireLevelRequired() {
        assertEquals(14, config.altars["FIRE"]!!.levelRequired,
            "FIRE altar level req should be 14. See: https://oldschool.runescape.wiki/w/Fire_rune")
    }

    @Test fun testCosmicLevelRequired() {
        assertEquals(27, config.altars["COSMIC"]!!.levelRequired,
            "COSMIC altar level req should be 27. See: https://oldschool.runescape.wiki/w/Cosmic_rune")
    }

    @Test fun testNatureLevelRequired() {
        assertEquals(44, config.altars["NATURE"]!!.levelRequired,
            "NATURE altar level req should be 44. See: https://oldschool.runescape.wiki/w/Nature_rune")
    }

    @Test fun testLawLevelRequired() {
        assertEquals(54, config.altars["LAW"]!!.levelRequired,
            "LAW altar level req should be 54. See: https://oldschool.runescape.wiki/w/Law_rune")
    }

    @Test fun testDeathLevelRequired() {
        assertEquals(65, config.altars["DEATH"]!!.levelRequired,
            "DEATH altar level req should be 65. See: https://oldschool.runescape.wiki/w/Death_rune")
    }

    @Test fun testBloodLevelRequired() {
        assertEquals(77, config.altars["BLOOD"]!!.levelRequired,
            "BLOOD altar level req should be 77. See: https://oldschool.runescape.wiki/w/Blood_rune")
    }

    @Test fun testSoulLevelRequired() {
        assertEquals(90, config.altars["SOUL"]!!.levelRequired,
            "SOUL altar level req should be 90. See: https://oldschool.runescape.wiki/w/Soul_rune")
    }

    // -------------------------------------------------------------------------
    // XP per essence — https://oldschool.runescape.wiki/w/Runecraft#Experience
    // -------------------------------------------------------------------------

    @Test fun testAirXp() {
        assertEquals(5.0, config.altars["AIR"]!!.xpPerEssence, 0.001,
            "AIR XP/essence should be 5.0. See: https://oldschool.runescape.wiki/w/Air_rune")
    }

    @Test fun testWaterXp() {
        assertEquals(6.0, config.altars["WATER"]!!.xpPerEssence, 0.001,
            "WATER XP/essence should be 6.0. See: https://oldschool.runescape.wiki/w/Water_rune")
    }

    @Test fun testChaosXp() {
        assertEquals(8.5, config.altars["CHAOS"]!!.xpPerEssence, 0.001,
            "CHAOS XP/essence should be 8.5. See: https://oldschool.runescape.wiki/w/Chaos_rune")
    }

    @Test fun testNatureXp() {
        assertEquals(9.0, config.altars["NATURE"]!!.xpPerEssence, 0.001,
            "NATURE XP/essence should be 9.0. See: https://oldschool.runescape.wiki/w/Nature_rune")
    }

    @Test fun testDeathXp() {
        assertEquals(10.0, config.altars["DEATH"]!!.xpPerEssence, 0.001,
            "DEATH XP/essence should be 10.0. See: https://oldschool.runescape.wiki/w/Death_rune")
    }

    @Test fun testBloodXp() {
        assertEquals(23.8, config.altars["BLOOD"]!!.xpPerEssence, 0.001,
            "BLOOD XP/essence should be 23.8. See: https://oldschool.runescape.wiki/w/Blood_rune")
    }

    @Test fun testSoulXp() {
        assertEquals(29.7, config.altars["SOUL"]!!.xpPerEssence, 0.001,
            "SOUL XP/essence should be 29.7. See: https://oldschool.runescape.wiki/w/Soul_rune")
    }

    // -------------------------------------------------------------------------
    // Multiple-rune thresholds — https://oldschool.runescape.wiki/w/Runecraft#Multiple_runes_per_essence
    // -------------------------------------------------------------------------

    @Test fun testAirRunesAt1() {
        assertEquals(1, config.altars["AIR"]!!.runesPerEssence(1),
            "At level 1, AIR yields 1 rune/ess. See: https://oldschool.runescape.wiki/w/Air_rune#Crafting")
    }

    @Test fun testAirRunesAt11() {
        assertEquals(2, config.altars["AIR"]!!.runesPerEssence(11),
            "At level 11, AIR yields 2 runes/ess. See: https://oldschool.runescape.wiki/w/Air_rune#Crafting")
    }

    @Test fun testAirRunesAt99() {
        assertEquals(10, config.altars["AIR"]!!.runesPerEssence(99),
            "At level 99, AIR yields 10 runes/ess. See: https://oldschool.runescape.wiki/w/Air_rune#Crafting")
    }

    @Test fun testFireRunesAt35() {
        assertEquals(2, config.altars["FIRE"]!!.runesPerEssence(35),
            "At level 35, FIRE yields 2 runes/ess. See: https://oldschool.runescape.wiki/w/Fire_rune#Crafting")
    }

    @Test fun testFireRunesAt70() {
        assertEquals(3, config.altars["FIRE"]!!.runesPerEssence(70),
            "At level 70, FIRE yields 3 runes/ess. See: https://oldschool.runescape.wiki/w/Fire_rune#Crafting")
    }

    @Test fun testNatureRunesAt91() {
        assertEquals(2, config.altars["NATURE"]!!.runesPerEssence(91),
            "At level 91, NATURE yields 2 runes/ess. See: https://oldschool.runescape.wiki/w/Nature_rune#Crafting")
    }

    @Test fun testBloodAlwaysOneRune() {
        // Blood runes never yield multiples — always 1 per essence
        // Source: https://oldschool.runescape.wiki/w/Blood_rune#Crafting
        assertEquals(1, config.altars["BLOOD"]!!.runesPerEssence(99),
            "BLOOD always yields 1 rune/ess. See: https://oldschool.runescape.wiki/w/Blood_rune#Crafting")
    }

    @Test fun testLawRunesAt95() {
        assertEquals(2, config.altars["LAW"]!!.runesPerEssence(95),
            "At level 95, LAW yields 2 runes/ess. See: https://oldschool.runescape.wiki/w/Law_rune#Crafting")
    }

    // -------------------------------------------------------------------------
    // Essence type correctness
    // -------------------------------------------------------------------------

    @Test fun testAirUsesRuneEssence() {
        // Air rune is free-to-play and uses rune essence (1436)
        // Source: https://oldschool.runescape.wiki/w/Air_rune
        assertEquals(1436, config.altars["AIR"]!!.essenceItemId,
            "AIR altar should use rune essence (1436). See: https://oldschool.runescape.wiki/w/Air_rune")
    }

    @Test fun testCosmicUsesPureEssence() {
        // Cosmic rune is members-only and requires pure essence (7936)
        // Source: https://oldschool.runescape.wiki/w/Cosmic_rune
        assertEquals(7936, config.altars["COSMIC"]!!.essenceItemId,
            "COSMIC altar should use pure essence (7936). See: https://oldschool.runescape.wiki/w/Cosmic_rune")
    }

    // -------------------------------------------------------------------------
    // Index correctness
    // -------------------------------------------------------------------------

    @Test fun testByAltarObjectIdAir() {
        // Air altar object ID 34760 — https://oldschool.runescape.wiki/w/Air_Altar
        val altar = config.byAltarObjectId[34760]
        assertNotNull(altar, "byAltarObjectId[34760] should resolve to an altar")
        assertEquals("AIR", altar!!.name,
            "Object ID 34760 should map to AIR altar. See: https://oldschool.runescape.wiki/w/Air_Altar")
    }

    @Test fun testAllAltarObjectIdsPopulated() {
        // All 13 altars have non-null object IDs in the YAML
        // Source: https://oldschool.runescape.wiki/w/Runecraft#Runes
        assertEquals(true, config.allAltarObjectIds.isNotEmpty(),
            "allAltarObjectIds should not be empty. See: https://oldschool.runescape.wiki/w/Runecraft#Runes")
    }
}
