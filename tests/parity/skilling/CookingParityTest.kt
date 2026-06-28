package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import skills.cooking.CookingConfig
import skills.cooking.CookingLoader
import java.nio.file.Path

/**
 * Parity tests for Cooking skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Cooking
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CookingParityTest {

    private lateinit var config: CookingConfig

    @BeforeAll
    fun loadData() {
        config = CookingLoader.load(Path.of("data/skills/cooking.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * 4 ticks per cooking attempt (2.4 seconds).
     * Source: https://oldschool.runescape.wiki/w/Cooking#Mechanics
     */
    @Test fun `tick rate is 4 ticks per attempt`() {
        assertEquals(4, config.meta.ticksPerAttempt,
            "Cooking ticks_per_attempt must be 4. See: https://oldschool.runescape.wiki/w/Cooking#Mechanics")
    }

    /**
     * Cooking gauntlets item ID is 775.
     * Source: https://oldschool.runescape.wiki/w/Cooking_gauntlets
     */
    @Test fun `cooking gauntlets item id is 775`() {
        assertEquals(775, config.meta.cookingGauntletsItemId,
            "cooking_gauntlets_item_id must be 775. See: https://oldschool.runescape.wiki/w/Cooking_gauntlets")
    }

    // -------------------------------------------------------------------------
    // Food XP values — Source: https://oldschool.runescape.wiki/w/Cooking#Cooking_food
    // -------------------------------------------------------------------------

    @Test fun `shrimps xp is 30_0`() = assertFoodXp("SHRIMPS", 30.0,
        "https://oldschool.runescape.wiki/w/Shrimps")

    @Test fun `sardine xp is 40_0`() = assertFoodXp("SARDINE", 40.0,
        "https://oldschool.runescape.wiki/w/Sardine")

    @Test fun `anchovies xp is 30_0`() = assertFoodXp("ANCHOVIES", 30.0,
        "https://oldschool.runescape.wiki/w/Anchovies")

    @Test fun `herring xp is 50_0`() = assertFoodXp("HERRING", 50.0,
        "https://oldschool.runescape.wiki/w/Herring")

    @Test fun `mackerel xp is 60_0`() = assertFoodXp("MACKEREL", 60.0,
        "https://oldschool.runescape.wiki/w/Mackerel")

    @Test fun `trout xp is 70_0`() = assertFoodXp("TROUT", 70.0,
        "https://oldschool.runescape.wiki/w/Trout")

    @Test fun `cod xp is 75_0`() = assertFoodXp("COD", 75.0,
        "https://oldschool.runescape.wiki/w/Cod")

    @Test fun `pike xp is 80_0`() = assertFoodXp("PIKE", 80.0,
        "https://oldschool.runescape.wiki/w/Pike")

    @Test fun `salmon xp is 90_0`() = assertFoodXp("SALMON", 90.0,
        "https://oldschool.runescape.wiki/w/Salmon")

    @Test fun `tuna xp is 100_0`() = assertFoodXp("TUNA", 100.0,
        "https://oldschool.runescape.wiki/w/Tuna")

    @Test fun `lobster xp is 120_0`() = assertFoodXp("LOBSTER", 120.0,
        "https://oldschool.runescape.wiki/w/Lobster")

    @Test fun `bass xp is 130_0`() = assertFoodXp("BASS", 130.0,
        "https://oldschool.runescape.wiki/w/Bass")

    @Test fun `swordfish xp is 140_0`() = assertFoodXp("SWORDFISH", 140.0,
        "https://oldschool.runescape.wiki/w/Swordfish")

    @Test fun `monkfish xp is 150_0`() = assertFoodXp("MONKFISH", 150.0,
        "https://oldschool.runescape.wiki/w/Monkfish")

    @Test fun `karambwan xp is 190_0`() = assertFoodXp("KARAMBWAN", 190.0,
        "https://oldschool.runescape.wiki/w/Cooked_karambwan")

    @Test fun `shark xp is 210_0`() = assertFoodXp("SHARK", 210.0,
        "https://oldschool.runescape.wiki/w/Shark")

    @Test fun `sea_turtle xp is 211_3`() = assertFoodXp("SEA_TURTLE", 211.3,
        "https://oldschool.runescape.wiki/w/Sea_turtle")

    @Test fun `manta_ray xp is 216_3`() = assertFoodXp("MANTA_RAY", 216.3,
        "https://oldschool.runescape.wiki/w/Manta_ray")

    // -------------------------------------------------------------------------
    // Level requirements — Source: https://oldschool.runescape.wiki/w/Cooking#Cooking_food
    // -------------------------------------------------------------------------

    @Test fun `shrimps requires level 1`() = assertFoodLevel("SHRIMPS", 1)
    @Test fun `sardine requires level 1`() = assertFoodLevel("SARDINE", 1)
    @Test fun `anchovies requires level 1`() = assertFoodLevel("ANCHOVIES", 1)
    @Test fun `herring requires level 5`() = assertFoodLevel("HERRING", 5)
    @Test fun `mackerel requires level 10`() = assertFoodLevel("MACKEREL", 10)
    @Test fun `trout requires level 15`() = assertFoodLevel("TROUT", 15)
    @Test fun `cod requires level 18`() = assertFoodLevel("COD", 18)
    @Test fun `pike requires level 20`() = assertFoodLevel("PIKE", 20)
    @Test fun `salmon requires level 25`() = assertFoodLevel("SALMON", 25)
    @Test fun `tuna requires level 30`() = assertFoodLevel("TUNA", 30)
    @Test fun `karambwan requires level 30`() = assertFoodLevel("KARAMBWAN", 30)
    @Test fun `lobster requires level 40`() = assertFoodLevel("LOBSTER", 40)
    @Test fun `bass requires level 43`() = assertFoodLevel("BASS", 43)
    @Test fun `swordfish requires level 45`() = assertFoodLevel("SWORDFISH", 45)
    @Test fun `monkfish requires level 62`() = assertFoodLevel("MONKFISH", 62)
    @Test fun `shark requires level 80`() = assertFoodLevel("SHARK", 80)
    @Test fun `sea_turtle requires level 82`() = assertFoodLevel("SEA_TURTLE", 82)
    @Test fun `manta_ray requires level 91`() = assertFoodLevel("MANTA_RAY", 91)

    // -------------------------------------------------------------------------
    // Cooking gauntlets — Source: https://oldschool.runescape.wiki/w/Cooking_gauntlets
    // -------------------------------------------------------------------------

    @Test fun `lobster uses cooking gauntlets`() {
        val food = config.food["LOBSTER"]!!
        assertTrue(food.usesGauntlets,
            "Lobster must have uses_gauntlets=true. See: https://oldschool.runescape.wiki/w/Cooking_gauntlets")
    }

    @Test fun `lobster gauntlets stop burn level is 64`() {
        val food = config.food["LOBSTER"]!!
        assertEquals(64, food.gauntletsStopBurnLevel,
            "Lobster gauntlets_stop_burn_level must be 64. See: https://oldschool.runescape.wiki/w/Cooking_gauntlets")
    }

    @Test fun `swordfish uses cooking gauntlets`() {
        val food = config.food["SWORDFISH"]!!
        assertTrue(food.usesGauntlets,
            "Swordfish must have uses_gauntlets=true. See: https://oldschool.runescape.wiki/w/Cooking_gauntlets")
    }

    @Test fun `swordfish gauntlets stop burn level is 81`() {
        val food = config.food["SWORDFISH"]!!
        assertEquals(81, food.gauntletsStopBurnLevel,
            "Swordfish gauntlets_stop_burn_level must be 81. See: https://oldschool.runescape.wiki/w/Cooking_gauntlets")
    }

    @Test fun `shark uses cooking gauntlets`() {
        val food = config.food["SHARK"]!!
        assertTrue(food.usesGauntlets,
            "Shark must have uses_gauntlets=true. See: https://oldschool.runescape.wiki/w/Cooking_gauntlets")
    }

    @Test fun `shark gauntlets stop burn level is 94`() {
        val food = config.food["SHARK"]!!
        assertEquals(94, food.gauntletsStopBurnLevel,
            "Shark gauntlets_stop_burn_level must be 94. See: https://oldschool.runescape.wiki/w/Cooking_gauntlets")
    }

    // -------------------------------------------------------------------------
    // Stop-burn levels — Source: https://oldschool.runescape.wiki/w/Cooking#Burn_level
    // -------------------------------------------------------------------------

    @Test fun `lobster stop burn range is 74`() {
        val food = config.food["LOBSTER"]!!
        assertEquals(74, food.stopBurnLevelRange,
            "Lobster stop_burn_level_range must be 74. See: https://oldschool.runescape.wiki/w/Lobster")
    }

    @Test fun `shark stop burn range is 99`() {
        val food = config.food["SHARK"]!!
        assertEquals(99, food.stopBurnLevelRange,
            "Shark stop_burn_level_range must be 99. See: https://oldschool.runescape.wiki/w/Shark")
    }

    @Test fun `monkfish stop burn range is 90`() {
        val food = config.food["MONKFISH"]!!
        assertEquals(90, food.stopBurnLevelRange,
            "Monkfish stop_burn_level_range must be 90. See: https://oldschool.runescape.wiki/w/Monkfish")
    }

    // -------------------------------------------------------------------------
    // byRawItemId lookup
    // -------------------------------------------------------------------------

    @Test fun `byRawItemId resolves raw shrimps`() {
        val food = config.byRawItemId[317]
        assertEquals("SHRIMPS", food?.name,
            "Raw shrimps (id 317) must resolve to SHRIMPS. See: https://oldschool.runescape.wiki/w/Raw_shrimps")
    }

    @Test fun `byRawItemId resolves raw shark`() {
        val food = config.byRawItemId[383]
        assertEquals("SHARK", food?.name,
            "Raw shark (id 383) must resolve to SHARK. See: https://oldschool.runescape.wiki/w/Raw_shark")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertFoodXp(foodName: String, expectedXp: Double, source: String) {
        val food = config.food[foodName]
            ?: error("Food '$foodName' not found in cooking.yaml")
        assertEquals(expectedXp, food.xp,
            "$foodName XP should be $expectedXp. See: $source")
    }

    private fun assertFoodLevel(foodName: String, expectedLevel: Int) {
        val food = config.food[foodName]
            ?: error("Food '$foodName' not found in cooking.yaml")
        assertEquals(expectedLevel, food.levelRequired,
            "$foodName level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Cooking#Cooking_food")
    }
}
