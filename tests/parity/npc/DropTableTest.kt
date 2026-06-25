package parity.npc

import npc.DropEntry
import npc.DropTableRoller
import npc.NpcDefinitions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs

/**
 * Parity tests for NPC drop table rolling logic.
 * Source: https://oldschool.runescape.wiki/w/Drop_rate
 */
class DropTableTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            NpcDefinitions.init(Path.of("data/osrsbox"))
        }
    }

    private fun dataPresent() = Files.exists(Path.of("data/osrsbox/monsters-complete.json"))

    // -------------------------------------------------------------------------
    // Test 1: Always-drop (rarity=1.0) always returns the item.
    // Source: https://oldschool.runescape.wiki/w/Drop_rate
    // -------------------------------------------------------------------------

    @Test fun `always-drop entry always rolls true`() {
        assertTrue(
            DropTableRoller.shouldDrop(1.0),
            "rarity=1.0 must always drop. Source: https://oldschool.runescape.wiki/w/Drop_rate"
        )
    }

    @Test fun `always-drop item is always in roll result`() {
        val bones = DropEntry(itemId = 526, minQty = 1, maxQty = 1, rarity = 1.0, noted = false, rolls = 1)
        repeat(100) {
            val result = DropTableRoller.roll(listOf(bones))
            assertTrue(
                result.any { it.first == 526 },
                "Bones (id=526) with rarity=1.0 must always appear. Source: https://oldschool.runescape.wiki/w/Drop_rate"
            )
        }
    }

    // -------------------------------------------------------------------------
    // Test 2: Zero-rarity entries never drop.
    // Source: https://oldschool.runescape.wiki/w/Drop_rate
    // -------------------------------------------------------------------------

    @Test fun `zero-rarity entry never rolls true`() {
        assertFalse(
            DropTableRoller.shouldDrop(0.0),
            "rarity=0.0 must never drop. Source: https://oldschool.runescape.wiki/w/Drop_rate"
        )
    }

    @Test fun `zero-rarity item never appears in roll result`() {
        val placeholder = DropEntry(itemId = 999, minQty = 1, maxQty = 1, rarity = 0.0, noted = false, rolls = 1)
        repeat(100) {
            val result = DropTableRoller.roll(listOf(placeholder))
            assertTrue(
                result.none { it.first == 999 },
                "rarity=0.0 item must never appear. Source: https://oldschool.runescape.wiki/w/Drop_rate"
            )
        }
    }

    // -------------------------------------------------------------------------
    // Test 3: Correct denominator for 1/128 rarity.
    // floor(1.0 / 0.0078125) = floor(128.0) = 128.
    // Source: https://oldschool.runescape.wiki/w/Drop_rate
    // -------------------------------------------------------------------------

    @Test fun `1 in 128 rarity gives approximately 1 in 128 drop rate`() {
        // Source: https://oldschool.runescape.wiki/w/Drop_rate
        val entry = DropEntry(itemId = 1, minQty = 1, maxQty = 1, rarity = 0.0078125, noted = false, rolls = 1)
        val trials = 128_000
        var hits = 0
        repeat(trials) { if (DropTableRoller.shouldDrop(0.0078125)) hits++ }
        val rate = hits.toDouble() / trials
        val expected = 1.0 / 128.0
        assertTrue(
            abs(rate - expected) < 0.005,
            "1/128 rarity should produce ~${expected} drop rate; got $rate. " +
            "Source: https://oldschool.runescape.wiki/w/Drop_rate"
        )
    }

    // -------------------------------------------------------------------------
    // Test 4 & 5: Quantity range and fixed quantity parsing.
    // Source: https://oldschool.runescape.wiki/w/Drop_rate
    // -------------------------------------------------------------------------

    @Test fun `fixed quantity entry always yields exact amount`() {
        // Source: https://oldschool.runescape.wiki/w/Drop_rate
        val entry = DropEntry(itemId = 995, minQty = 5, maxQty = 5, rarity = 1.0, noted = false, rolls = 1)
        repeat(50) {
            val result = DropTableRoller.roll(listOf(entry))
            assertEquals(1, result.size)
            assertEquals(5, result[0].second, "Fixed qty=5 must always yield 5")
        }
    }

    @Test fun `quantity range stays within declared bounds`() {
        // Source: https://oldschool.runescape.wiki/w/Drop_rate
        val entry = DropEntry(itemId = 882, minQty = 1, maxQty = 6, rarity = 1.0, noted = false, rolls = 1)
        repeat(200) {
            val result = DropTableRoller.roll(listOf(entry))
            val qty = result[0].second
            assertTrue(qty in 1..6, "Quantity $qty must be in range 1-6")
        }
    }

    // -------------------------------------------------------------------------
    // Test 6: rolls=2 triggers two independent checks.
    // Source: https://oldschool.runescape.wiki/w/Drop_rate
    // -------------------------------------------------------------------------

    @Test fun `rolls=2 can yield up to two copies of the same item`() {
        // With rarity=1.0 and rolls=2, we always get exactly 2 results.
        // Source: https://oldschool.runescape.wiki/w/Drop_rate
        val entry = DropEntry(itemId = 526, minQty = 1, maxQty = 1, rarity = 1.0, noted = false, rolls = 2)
        val result = DropTableRoller.roll(listOf(entry))
        assertEquals(
            2, result.size,
            "rolls=2 with rarity=1.0 must produce 2 drop results. Source: https://oldschool.runescape.wiki/w/Drop_rate"
        )
    }

    // -------------------------------------------------------------------------
    // Test 7: Statistical — 1/2 rarity ≈ 50% over many trials.
    // Tolerance: ≤ 0.02 (2%) per tests-parity.md.
    // Source: https://oldschool.runescape.wiki/w/Drop_rate
    // -------------------------------------------------------------------------

    @Test fun `1 in 2 rarity drops approximately half the time`() {
        val trials = 10_000
        var hits = 0
        repeat(trials) { if (DropTableRoller.shouldDrop(0.5)) hits++ }
        val rate = hits.toDouble() / trials
        assertTrue(
            abs(rate - 0.5) < 0.02,
            "1/2 rarity should drop ~50%; got ${rate * 100}%. " +
            "Source: https://oldschool.runescape.wiki/w/Drop_rate"
        )
    }

    // -------------------------------------------------------------------------
    // Test 8: osrsbox Goblin has Bones (id=526) as an always-drop.
    // Source: https://oldschool.runescape.wiki/w/Goblin#Drops
    // -------------------------------------------------------------------------

    @Test fun `goblin always drops bones`() {
        assumeTrue(dataPresent(), "monsters-complete.json absent — skipping")
        // osrsbox Goblin id=655.  Source: https://oldschool.runescape.wiki/w/Goblin#Drops
        val def = NpcDefinitions.get(655)
        assertNotNull(def, "Goblin (id=655) must be present in osrsbox")
        val bonesDrop = def!!.drops.firstOrNull { it.itemId == 526 }
        assertNotNull(bonesDrop, "Goblin must have a Bones drop entry (id=526)")
        assertEquals(
            1.0, bonesDrop!!.rarity,
            "Goblin Bones must have rarity=1.0 (always). Source: https://oldschool.runescape.wiki/w/Goblin#Drops"
        )
    }
}
