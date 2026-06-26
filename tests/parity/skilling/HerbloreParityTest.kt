package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import skills.herblore.HerbloreConfig
import skills.herblore.HerbloreLoader
import java.nio.file.Path

/**
 * Parity tests for Herblore skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Herblore
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HerbloreParityTest {

    private lateinit var config: HerbloreConfig

    @BeforeAll
    fun loadData() {
        config = HerbloreLoader.load(Path.of("data/skills/herblore.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta — Source: https://oldschool.runescape.wiki/w/Vial_of_water
    // -------------------------------------------------------------------------

    @Test fun `vial of water item id is 227`() {
        assertEquals(227, config.meta.vialOfWaterItemId,
            "Vial of water item ID must be 227. See: https://oldschool.runescape.wiki/w/Vial_of_water")
    }

    // -------------------------------------------------------------------------
    // Herb cleaning XP — Source: https://oldschool.runescape.wiki/w/Herblore#Cleaning_herbs
    // -------------------------------------------------------------------------

    @Test fun `guam clean xp is 2_5`() = assertCleanXp("GUAM", 2.5)
    @Test fun `marrentill clean xp is 3_8`() = assertCleanXp("MARRENTILL", 3.8)
    @Test fun `tarromin clean xp is 5_0`() = assertCleanXp("TARROMIN", 5.0)
    @Test fun `harralander clean xp is 6_3`() = assertCleanXp("HARRALANDER", 6.3)
    @Test fun `ranarr weed clean xp is 7_5`() = assertCleanXp("RANARR_WEED", 7.5)
    @Test fun `toadflax clean xp is 8_0`() = assertCleanXp("TOADFLAX", 8.0)
    @Test fun `irit leaf clean xp is 8_8`() = assertCleanXp("IRIT_LEAF", 8.8)
    @Test fun `avantoe clean xp is 10_0`() = assertCleanXp("AVANTOE", 10.0)
    @Test fun `kwuarm clean xp is 11_3`() = assertCleanXp("KWUARM", 11.3)
    @Test fun `snapdragon clean xp is 11_8`() = assertCleanXp("SNAPDRAGON", 11.8)
    @Test fun `cadantine clean xp is 12_5`() = assertCleanXp("CADANTINE", 12.5)
    @Test fun `lantadyme clean xp is 13_1`() = assertCleanXp("LANTADYME", 13.1)
    @Test fun `dwarf weed clean xp is 13_8`() = assertCleanXp("DWARF_WEED", 13.8)
    @Test fun `torstol clean xp is 15_0`() = assertCleanXp("TORSTOL", 15.0)

    // -------------------------------------------------------------------------
    // Herb cleaning level requirements — Source: https://oldschool.runescape.wiki/w/Herblore#Cleaning_herbs
    // -------------------------------------------------------------------------

    @Test fun `guam requires level 1`() = assertCleanLevel("GUAM", 1)
    @Test fun `marrentill requires level 5`() = assertCleanLevel("MARRENTILL", 5)
    @Test fun `tarromin requires level 11`() = assertCleanLevel("TARROMIN", 11)
    @Test fun `ranarr weed requires level 25`() = assertCleanLevel("RANARR_WEED", 25)
    @Test fun `snapdragon requires level 59`() = assertCleanLevel("SNAPDRAGON", 59)
    @Test fun `torstol requires level 75`() = assertCleanLevel("TORSTOL", 75)

    // -------------------------------------------------------------------------
    // Herb item IDs — Source: https://oldschool.runescape.wiki/w/Herblore
    // -------------------------------------------------------------------------

    @Test fun `guam grimy id is 199 clean id is 249 unf id is 91`() {
        val h = config.herbs["GUAM"]!!
        assertEquals(199, h.grimyItemId, "Grimy guam ID must be 199")
        assertEquals(249, h.cleanItemId, "Clean guam ID must be 249")
        assertEquals(91, h.unfPotionItemId, "Guam potion (unf) ID must be 91")
    }

    @Test fun `ranarr grimy id is 207 clean id is 257 unf id is 99`() {
        val h = config.herbs["RANARR_WEED"]!!
        assertEquals(207, h.grimyItemId, "Grimy ranarr ID must be 207")
        assertEquals(257, h.cleanItemId, "Clean ranarr ID must be 257")
        assertEquals(99, h.unfPotionItemId, "Ranarr potion (unf) ID must be 99")
    }

    // -------------------------------------------------------------------------
    // Potion XP — Source: https://oldschool.runescape.wiki/w/Herblore#Potions
    // -------------------------------------------------------------------------

    @Test fun `attack potion xp is 25_0`() = assertPotionXp("ATTACK_POTION", 25.0)
    @Test fun `antipoison xp is 37_5`() = assertPotionXp("ANTIPOISON", 37.5)
    @Test fun `prayer potion xp is 87_5`() = assertPotionXp("PRAYER_POTION", 87.5)
    @Test fun `super attack xp is 100_0`() = assertPotionXp("SUPER_ATTACK", 100.0)
    @Test fun `super strength xp is 125_0`() = assertPotionXp("SUPER_STRENGTH", 125.0)
    @Test fun `ranging potion xp is 162_5`() = assertPotionXp("RANGING_POTION", 162.5)
    @Test fun `saradomin brew xp is 180_0`() = assertPotionXp("SARADOMIN_BREW", 180.0)

    // -------------------------------------------------------------------------
    // Potion level requirements — Source: https://oldschool.runescape.wiki/w/Herblore#Potions
    // -------------------------------------------------------------------------

    @Test fun `attack potion requires level 3`() = assertPotionLevel("ATTACK_POTION", 3)
    @Test fun `prayer potion requires level 38`() = assertPotionLevel("PRAYER_POTION", 38)
    @Test fun `super strength requires level 55`() = assertPotionLevel("SUPER_STRENGTH", 55)
    @Test fun `magic potion requires level 76`() = assertPotionLevel("MAGIC_POTION", 76)
    @Test fun `saradomin brew requires level 81`() = assertPotionLevel("SARADOMIN_BREW", 81)

    // -------------------------------------------------------------------------
    // Potion outputs are 3-dose (freshly-made). Source: https://oldschool.runescape.wiki/w/Herblore#Potions
    // -------------------------------------------------------------------------

    @Test fun `attack potion output is 3-dose id 121`() {
        assertEquals(121, config.potions["ATTACK_POTION"]!!.outputItemId,
            "Attack potion output must be the (3) dose variant, id 121. " +
            "See: https://oldschool.runescape.wiki/w/Attack_potion")
    }

    @Test fun `prayer potion output is 3-dose id 139`() {
        assertEquals(139, config.potions["PRAYER_POTION"]!!.outputItemId,
            "Prayer potion output must be the (3) dose variant, id 139. " +
            "See: https://oldschool.runescape.wiki/w/Prayer_potion")
    }

    @Test fun `zamorak brew output is 3-dose id 189`() {
        assertEquals(189, config.potions["ZAMORAK_BREW"]!!.outputItemId,
            "Zamorak brew output must be the (3) dose variant, id 189. " +
            "See: https://oldschool.runescape.wiki/w/Zamorak_brew")
    }

    // -------------------------------------------------------------------------
    // Recipe lookups
    // -------------------------------------------------------------------------

    @Test fun `byGrimyId lookup finds guam by id 199`() {
        val herb = config.byGrimyId[199] ?: error("byGrimyId[199] must resolve to GUAM")
        assertEquals("GUAM", herb.name,
            "Grimy ID 199 must map to GUAM. See: https://oldschool.runescape.wiki/w/Guam_leaf")
    }

    @Test fun `bySecondaryAndUnf lookup finds attack potion by eye of newt plus guam unf`() {
        // eye of newt (221) + guam potion unf (91) → attack potion
        val recipe = config.bySecondaryAndUnf[221 to 91]
            ?: error("bySecondaryAndUnf[(221,91)] must resolve to ATTACK_POTION")
        assertEquals("ATTACK_POTION", recipe.name,
            "Eye of newt + guam unf must make Attack potion. " +
            "See: https://oldschool.runescape.wiki/w/Attack_potion")
    }

    @Test fun `prayer potion uses ranarr unf plus snape grass`() {
        val recipe = config.potions["PRAYER_POTION"]!!
        assertEquals(99, recipe.unfinishedItemId, "Prayer potion uses ranarr potion (unf), id 99")
        assertEquals(231, recipe.secondaryItemId, "Prayer potion secondary is snape grass, id 231")
        assertEquals(257, recipe.primaryItemId, "Prayer potion primary is clean ranarr, id 257")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertCleanXp(herb: String, expected: Double) {
        val h = config.herbs[herb] ?: error("Herb '$herb' not found in herblore.yaml")
        assertEquals(expected, h.cleanXp,
            "$herb clean_xp should be $expected. " +
            "See: https://oldschool.runescape.wiki/w/Herblore#Cleaning_herbs")
    }

    private fun assertCleanLevel(herb: String, expected: Int) {
        val h = config.herbs[herb] ?: error("Herb '$herb' not found in herblore.yaml")
        assertEquals(expected, h.cleanLevel,
            "$herb clean_level should be $expected. " +
            "See: https://oldschool.runescape.wiki/w/Herblore#Cleaning_herbs")
    }

    private fun assertPotionXp(potion: String, expected: Double) {
        val p = config.potions[potion] ?: error("Potion '$potion' not found in herblore.yaml")
        assertEquals(expected, p.xp,
            "$potion xp should be $expected. " +
            "See: https://oldschool.runescape.wiki/w/Herblore#Potions")
    }

    private fun assertPotionLevel(potion: String, expected: Int) {
        val p = config.potions[potion] ?: error("Potion '$potion' not found in herblore.yaml")
        assertEquals(expected, p.levelRequired,
            "$potion level_required should be $expected. " +
            "See: https://oldschool.runescape.wiki/w/Herblore#Potions")
    }
}
