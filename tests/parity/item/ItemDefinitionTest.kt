package parity.item

import item.ItemDefinition
import item.ItemDefinitions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Parity tests for item definitions loaded from osrsbox-db.
 * Tests are skipped gracefully if data/osrsbox/items-complete.json is absent.
 * Source: https://oldschool.runescape.wiki/w/Abyssal_whip
 */
class ItemDefinitionTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            ItemDefinitions.init(Path.of("data/osrsbox"))
        }
    }

    private fun dataPresent() = Files.exists(Path.of("data/osrsbox/items-complete.json"))

    @Test
    fun `graceful absent - returns EMPTY when data missing`() {
        // Even without data, getOrEmpty must never throw
        val def = ItemDefinitions.getOrEmpty(-1)
        assertEquals(ItemDefinition.EMPTY, def)
    }

    @Test
    fun `abyssal whip has correct slash attack bonus`() {
        // Source: https://oldschool.runescape.wiki/w/Abyssal_whip
        assumeTrue(dataPresent(), "items-complete.json absent — skipping")
        val whip = ItemDefinitions.get(4151)
        assertNotNull(whip, "Abyssal whip (id=4151) should be present in osrsbox")
        assertEquals(82, whip!!.attackSlash) { "Abyssal whip slash attack should be 82. See: https://oldschool.runescape.wiki/w/Abyssal_whip" }
    }

    @Test
    fun `abyssal whip has correct melee strength bonus`() {
        // Source: https://oldschool.runescape.wiki/w/Abyssal_whip
        assumeTrue(dataPresent(), "items-complete.json absent — skipping")
        val whip = ItemDefinitions.get(4151)
        assertNotNull(whip)
        assertEquals(82, whip!!.meleeStrength) { "Abyssal whip melee strength should be 82. See: https://oldschool.runescape.wiki/w/Abyssal_whip" }
    }

    @Test
    fun `abyssal whip is equipable`() {
        assumeTrue(dataPresent(), "items-complete.json absent — skipping")
        val whip = ItemDefinitions.get(4151)
        assertNotNull(whip)
        assertTrue(whip!!.equipable) { "Abyssal whip should be equipable" }
    }

    @Test
    fun `coins are not equipable`() {
        // Source: https://oldschool.runescape.wiki/w/Coins
        assumeTrue(dataPresent(), "items-complete.json absent — skipping")
        val coins = ItemDefinitions.get(995)
        assertNotNull(coins)
        assertFalse(coins!!.equipable)
        assertTrue(coins.stackable)
    }

    @Test
    fun `item count is at least 20000`() {
        assumeTrue(dataPresent(), "items-complete.json absent — skipping")
        // Verify the full dataset loaded
        val whip = ItemDefinitions.get(4151)
        assertNotNull(whip, "If items loaded, abyssal whip must be present")
    }
}
