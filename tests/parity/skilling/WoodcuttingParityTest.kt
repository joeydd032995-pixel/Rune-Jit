package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import skills.woodcutting.WoodcuttingConfig
import skills.woodcutting.WoodcuttingLoader
import java.nio.file.Path

/**
 * Parity tests for Woodcutting skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Woodcutting
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WoodcuttingParityTest {

    private lateinit var config: WoodcuttingConfig

    @BeforeAll
    fun loadData() {
        config = WoodcuttingLoader.load(Path.of("data/skills/woodcutting.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * 4 ticks per chop attempt (2.4 seconds).
     * Source: https://oldschool.runescape.wiki/w/Woodcutting#Mechanics
     */
    @Test fun `tick rate is 4 ticks per attempt`() {
        assertEquals(4, config.meta.ticksPerAttempt,
            "Woodcutting ticks_per_attempt must be 4. See: https://oldschool.runescape.wiki/w/Woodcutting#Mechanics")
    }

    // -------------------------------------------------------------------------
    // Tree XP values — Source: https://oldschool.runescape.wiki/w/Woodcutting#Experience
    // -------------------------------------------------------------------------

    @Test fun `normal tree xp is 25_0`() = assertTreeXp("NORMAL", 25.0,
        "https://oldschool.runescape.wiki/w/Logs")

    @Test fun `oak tree xp is 37_5`() = assertTreeXp("OAK", 37.5,
        "https://oldschool.runescape.wiki/w/Oak_logs")

    @Test fun `willow tree xp is 67_5`() = assertTreeXp("WILLOW", 67.5,
        "https://oldschool.runescape.wiki/w/Willow_logs")

    @Test fun `teak tree xp is 85_0`() = assertTreeXp("TEAK", 85.0,
        "https://oldschool.runescape.wiki/w/Teak_logs")

    @Test fun `maple tree xp is 100_0`() = assertTreeXp("MAPLE", 100.0,
        "https://oldschool.runescape.wiki/w/Maple_logs")

    @Test fun `mahogany tree xp is 125_0`() = assertTreeXp("MAHOGANY", 125.0,
        "https://oldschool.runescape.wiki/w/Mahogany_logs")

    @Test fun `arctic pine tree xp is 40_0`() = assertTreeXp("ARCTIC_PINE", 40.0,
        "https://oldschool.runescape.wiki/w/Arctic_pine_logs")

    @Test fun `hollow tree xp is 82_5`() = assertTreeXp("HOLLOW", 82.5,
        "https://oldschool.runescape.wiki/w/Bark")

    @Test fun `blisterwood tree xp is 76_0`() = assertTreeXp("BLISTERWOOD", 76.0,
        "https://oldschool.runescape.wiki/w/Blisterwood_tree#Woodcutting")

    @Test fun `yew tree xp is 175_0`() = assertTreeXp("YEW", 175.0,
        "https://oldschool.runescape.wiki/w/Yew_logs")

    @Test fun `magic tree xp is 250_0`() = assertTreeXp("MAGIC", 250.0,
        "https://oldschool.runescape.wiki/w/Magic_logs")

    @Test fun `redwood tree xp is 380_0`() = assertTreeXp("REDWOOD", 380.0,
        "https://oldschool.runescape.wiki/w/Redwood_logs")

    // -------------------------------------------------------------------------
    // Tree level requirements — Source: https://oldschool.runescape.wiki/w/Woodcutting#Trees
    // -------------------------------------------------------------------------

    @Test fun `normal tree requires level 1`() = assertTreeLevel("NORMAL", 1)
    @Test fun `oak tree requires level 15`() = assertTreeLevel("OAK", 15)
    @Test fun `willow tree requires level 30`() = assertTreeLevel("WILLOW", 30)
    @Test fun `teak tree requires level 35`() = assertTreeLevel("TEAK", 35)
    @Test fun `maple tree requires level 45`() = assertTreeLevel("MAPLE", 45)
    @Test fun `hollow tree requires level 45`() = assertTreeLevel("HOLLOW", 45)
    @Test fun `mahogany tree requires level 50`() = assertTreeLevel("MAHOGANY", 50)
    @Test fun `arctic pine tree requires level 54`() = assertTreeLevel("ARCTIC_PINE", 54)
    @Test fun `yew tree requires level 60`() = assertTreeLevel("YEW", 60)
    @Test fun `blisterwood tree requires level 62`() = assertTreeLevel("BLISTERWOOD", 62)
    @Test fun `magic tree requires level 75`() = assertTreeLevel("MAGIC", 75)
    @Test fun `redwood tree requires level 90`() = assertTreeLevel("REDWOOD", 90)

    // -------------------------------------------------------------------------
    // Axe WC bonuses — Source: https://oldschool.runescape.wiki/w/Axe
    // -------------------------------------------------------------------------

    @Test fun `bronze axe wc bonus is 1`() = assertAxeBonus("BRONZE_AXE", 1)
    @Test fun `iron axe wc bonus is 2`() = assertAxeBonus("IRON_AXE", 2)
    @Test fun `steel axe wc bonus is 3`() = assertAxeBonus("STEEL_AXE", 3)
    @Test fun `black axe wc bonus is 4`() = assertAxeBonus("BLACK_AXE", 4)
    @Test fun `mithril axe wc bonus is 5`() = assertAxeBonus("MITHRIL_AXE", 5)
    @Test fun `adamant axe wc bonus is 6`() = assertAxeBonus("ADAMANT_AXE", 6)
    @Test fun `rune axe wc bonus is 7`() = assertAxeBonus("RUNE_AXE", 7)
    @Test fun `dragon axe wc bonus is 8`() = assertAxeBonus("DRAGON_AXE", 8)
    @Test fun `third age axe wc bonus is 8`() = assertAxeBonus("THIRD_AGE_AXE", 8)
    @Test fun `infernal axe wc bonus is 8`() = assertAxeBonus("INFERNAL_AXE", 8)
    @Test fun `crystal axe wc bonus is 9`() = assertAxeBonus("CRYSTAL_AXE", 9)

    // -------------------------------------------------------------------------
    // Bird nest config — Source: https://oldschool.runescape.wiki/w/Bird_nest#Obtaining
    // -------------------------------------------------------------------------

    /** 1/256 base chance per log received. */
    @Test fun `bird nest base drop chance is 256`() {
        assertEquals(256, config.birdNests.baseDropChance,
            "Bird nest drop chance must be 1/256. See: https://oldschool.runescape.wiki/w/Bird_nest#Obtaining")
    }

    // -------------------------------------------------------------------------
    // Blisterwood special behaviour
    // -------------------------------------------------------------------------

    /** Blisterwood does not drop logs — XP granted directly. */
    @Test fun `blisterwood drops no log`() {
        val tree = config.trees["BLISTERWOOD"]!!
        assertEquals(false, tree.dropsLog,
            "Blisterwood drops_log must be false. See: https://oldschool.runescape.wiki/w/Blisterwood_tree")
        assertEquals(null, tree.logItemId,
            "Blisterwood log_item_id must be null.")
    }

    /** Blisterwood never depletes. */
    @Test fun `blisterwood never depletes`() {
        val tree = config.trees["BLISTERWOOD"]!!
        assertEquals(null, tree.respawnTicks,
            "Blisterwood respawn_ticks must be null (permanent tree). See: https://oldschool.runescape.wiki/w/Blisterwood_tree")
    }

    // -------------------------------------------------------------------------
    // Infernal axe
    // -------------------------------------------------------------------------

    /** Infernal axe burns logs. Source: https://oldschool.runescape.wiki/w/Infernal_axe */
    @Test fun `infernal axe burns logs`() {
        val axe = config.axes.first { it.name == "INFERNAL_AXE" }
        assertEquals(true, axe.burnsLogs,
            "Infernal axe must have burns_logs=true. See: https://oldschool.runescape.wiki/w/Infernal_axe")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertTreeXp(treeName: String, expectedXp: Double, source: String) {
        val tree = config.trees[treeName]
            ?: error("Tree '$treeName' not found in woodcutting.yaml")
        assertEquals(expectedXp, tree.xp,
            "$treeName XP should be $expectedXp. See: $source")
    }

    private fun assertTreeLevel(treeName: String, expectedLevel: Int) {
        val tree = config.trees[treeName]
            ?: error("Tree '$treeName' not found in woodcutting.yaml")
        assertEquals(expectedLevel, tree.levelRequired,
            "$treeName level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Woodcutting#Trees")
    }

    private fun assertAxeBonus(axeName: String, expectedBonus: Int) {
        val axe = config.axes.firstOrNull { it.name == axeName }
            ?: error("Axe '$axeName' not found in woodcutting.yaml")
        assertEquals(expectedBonus, axe.wcBonus,
            "$axeName woodcutting_bonus should be $expectedBonus. See: https://oldschool.runescape.wiki/w/Axe")
    }
}
