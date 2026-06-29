package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import skills.fletching.FletchingConfig
import skills.fletching.FletchingLoader
import java.nio.file.Path

/**
 * Parity tests for Fletching skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Fletching
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FletchingParityTest {

    private lateinit var config: FletchingConfig

    @BeforeAll
    fun loadData() {
        config = FletchingLoader.load(Path.of("data/skills/fletching.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta — Source: https://oldschool.runescape.wiki/w/Fletching
    // -------------------------------------------------------------------------

    /**
     * 4 ticks per action (2.4 seconds).
     * Source: https://oldschool.runescape.wiki/w/Fletching
     */
    @Test fun `ticks per action is 4`() {
        assertEquals(4, config.meta.ticksPerAction,
            "Fletching ticks_per_action must be 4. See: https://oldschool.runescape.wiki/w/Fletching")
    }

    // -------------------------------------------------------------------------
    // Recipe count sanity check
    // -------------------------------------------------------------------------

    @Test fun `at least 30 recipes are defined`() {
        assertTrue(config.recipes.size >= 30,
            "Expected at least 30 fletching recipes but found ${config.recipes.size}. " +
            "See: https://oldschool.runescape.wiki/w/Fletching")
    }

    // -------------------------------------------------------------------------
    // Bow fletching — Source: https://oldschool.runescape.wiki/w/Fletching#Bows
    // -------------------------------------------------------------------------

    @Test fun `shortbow requires level 5 and awards 5_0 xp`() {
        val r = config.recipes["SHORTBOW"]!!
        assertEquals(5, r.levelRequired,
            "Shortbow level_required must be 5. See: https://oldschool.runescape.wiki/w/Shortbow")
        assertEquals(5.0, r.xp,
            "Shortbow xp must be 5.0. See: https://oldschool.runescape.wiki/w/Shortbow")
    }

    @Test fun `longbow requires level 10 and awards 10_0 xp`() {
        val r = config.recipes["LONGBOW"]!!
        assertEquals(10, r.levelRequired,
            "Longbow level_required must be 10. See: https://oldschool.runescape.wiki/w/Longbow")
        assertEquals(10.0, r.xp,
            "Longbow xp must be 10.0. See: https://oldschool.runescape.wiki/w/Longbow")
    }

    @Test fun `oak shortbow requires level 20 and awards 16_5 xp`() {
        val r = config.recipes["OAK_SHORTBOW"]!!
        assertEquals(20, r.levelRequired,
            "Oak shortbow level_required must be 20. See: https://oldschool.runescape.wiki/w/Oak_shortbow")
        assertEquals(16.5, r.xp,
            "Oak shortbow xp must be 16.5. See: https://oldschool.runescape.wiki/w/Oak_shortbow")
    }

    @Test fun `yew longbow requires level 70 and awards 75_0 xp`() {
        val r = config.recipes["YEW_LONGBOW"]!!
        assertEquals(70, r.levelRequired,
            "Yew longbow level_required must be 70. See: https://oldschool.runescape.wiki/w/Yew_longbow")
        assertEquals(75.0, r.xp,
            "Yew longbow xp must be 75.0. See: https://oldschool.runescape.wiki/w/Yew_longbow")
    }

    @Test fun `magic longbow requires level 85 and awards 91_5 xp`() {
        val r = config.recipes["MAGIC_LONGBOW"]!!
        assertEquals(85, r.levelRequired,
            "Magic longbow level_required must be 85. See: https://oldschool.runescape.wiki/w/Magic_longbow")
        assertEquals(91.5, r.xp,
            "Magic longbow xp must be 91.5. See: https://oldschool.runescape.wiki/w/Magic_longbow")
    }

    // -------------------------------------------------------------------------
    // Stringing — Source: https://oldschool.runescape.wiki/w/Fletching#Bows
    // -------------------------------------------------------------------------

    @Test fun `shortbow stringing requires level 5 xp 5_0 bowstring 1777 output 841`() {
        val r = config.recipes["SHORTBOW_STRING"]!!
        assertEquals(5, r.levelRequired,
            "Shortbow stringing level_required must be 5. See: https://oldschool.runescape.wiki/w/Shortbow")
        assertEquals(5.0, r.xp,
            "Shortbow stringing xp must be 5.0. See: https://oldschool.runescape.wiki/w/Shortbow")
        assertEquals(1777, r.primaryItemId,
            "Shortbow stringing primary must be bowstring (1777). See: https://oldschool.runescape.wiki/w/Bowstring")
        assertEquals(841, r.outputItemId,
            "Shortbow stringing output must be shortbow (841). See: https://oldschool.runescape.wiki/w/Shortbow")
    }

    // -------------------------------------------------------------------------
    // Arrows — Source: https://oldschool.runescape.wiki/w/Fletching#Arrows
    // -------------------------------------------------------------------------

    @Test fun `headless arrow requires level 1 and awards 1_0 xp`() {
        val r = config.recipes["HEADLESS_ARROW"]!!
        assertEquals(1, r.levelRequired,
            "Headless arrow level_required must be 1. See: https://oldschool.runescape.wiki/w/Headless_arrow")
        assertEquals(1.0, r.xp,
            "Headless arrow xp must be 1.0. See: https://oldschool.runescape.wiki/w/Headless_arrow")
    }

    @Test fun `bronze arrow requires level 1 and awards 1_3 xp`() {
        val r = config.recipes["BRONZE_ARROW"]!!
        assertEquals(1, r.levelRequired,
            "Bronze arrow level_required must be 1. See: https://oldschool.runescape.wiki/w/Bronze_arrow")
        assertEquals(1.3, r.xp,
            "Bronze arrow xp must be 1.3. See: https://oldschool.runescape.wiki/w/Bronze_arrow")
    }

    // -------------------------------------------------------------------------
    // Item pair lookup
    // -------------------------------------------------------------------------

    @Test fun `shortbow recipe is accessible by name in recipes map`() {
        // SHORTBOW and ARROW_SHAFT share the same (946, 1511) item pair — in OSRS a
        // dialog allows choosing. Here we verify the recipe exists by name.
        // Source: https://oldschool.runescape.wiki/w/Shortbow
        val r = config.recipes["SHORTBOW"]
            ?: error("SHORTBOW must be present in the recipes map")
        assertEquals(946, r.primaryItemId,
            "SHORTBOW primary must be knife (946). See: https://oldschool.runescape.wiki/w/Shortbow")
        assertEquals(1511, r.targetItemId,
            "SHORTBOW target must be normal logs (1511). See: https://oldschool.runescape.wiki/w/Shortbow")
        assertEquals(50, r.outputItemId,
            "SHORTBOW output must be shortbow (u) (50). See: https://oldschool.runescape.wiki/w/Shortbow")
    }

    @Test fun `byItemPair finds arrow shaft recipe by knife on normal logs`() {
        // Knife (946) on Normal logs (1511) also produces arrow shafts.
        // byItemPair keeps the last writer; ARROW_SHAFT follows SHORTBOW in iteration.
        // This test verifies the pair resolves to a valid recipe (either bow or shaft).
        // Source: https://oldschool.runescape.wiki/w/Arrow_shaft
        val r = config.byItemPair[946 to 1511]
            ?: error("byItemPair[(946,1511)] must resolve to a recipe (knife on normal logs)")
        assertTrue(r.name == "SHORTBOW" || r.name == "ARROW_SHAFT",
            "Knife (946) on normal logs (1511) must match SHORTBOW or ARROW_SHAFT. Found: ${r.name}. " +
            "See: https://oldschool.runescape.wiki/w/Fletching#Bows")
    }

    @Test fun `byItemPair finds shortbow stringing recipe by bowstring on shortbow_u`() {
        // Bowstring (1777) on Shortbow (u) (50) → Shortbow
        val r = config.byItemPair[1777 to 50]
            ?: error("byItemPair[(1777,50)] must resolve to SHORTBOW_STRING")
        assertEquals("SHORTBOW_STRING", r.name,
            "Bowstring (1777) on shortbow (u) (50) must match SHORTBOW_STRING recipe. " +
            "See: https://oldschool.runescape.wiki/w/Shortbow")
    }

    @Test fun `byItemPair finds headless arrow recipe by shaft on feather`() {
        // Arrow shaft (52) on Feather (314) → Headless arrow
        val r = config.byItemPair[52 to 314]
            ?: error("byItemPair[(52,314)] must resolve to HEADLESS_ARROW")
        assertEquals("HEADLESS_ARROW", r.name,
            "Arrow shaft (52) on feather (314) must match HEADLESS_ARROW recipe. " +
            "See: https://oldschool.runescape.wiki/w/Headless_arrow")
    }

    // -------------------------------------------------------------------------
    // Arrow shaft output quantities
    // -------------------------------------------------------------------------

    @Test fun `arrow shaft from normal logs produces 15 shafts`() {
        val r = config.recipes["ARROW_SHAFT"]!!
        assertEquals(15, r.outputQty,
            "Arrow shaft from normal logs must produce 15. " +
            "See: https://oldschool.runescape.wiki/w/Arrow_shaft")
    }

    @Test fun `oak arrow shaft produces 30 shafts`() {
        val r = config.recipes["OAK_ARROW_SHAFT"]!!
        assertEquals(30, r.outputQty,
            "Arrow shaft from oak logs must produce 30. " +
            "See: https://oldschool.runescape.wiki/w/Arrow_shaft")
    }
}
