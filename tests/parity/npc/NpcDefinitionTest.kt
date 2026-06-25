package parity.npc

import npc.NpcDefinition
import npc.NpcDefinitions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Parity tests for NPC definitions loaded from osrsbox-db.
 * Tests are skipped gracefully if data/osrsbox/monsters-complete.json is absent.
 * Source: https://oldschool.runescape.wiki/w/Cow
 */
class NpcDefinitionTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            NpcDefinitions.init(Path.of("data/osrsbox"))
        }
    }

    private fun dataPresent() = Files.exists(Path.of("data/osrsbox/monsters-complete.json"))

    @Test
    fun `graceful absent - returns EMPTY when data missing`() {
        val def = NpcDefinitions.getOrEmpty(-1)
        assertEquals(NpcDefinition.EMPTY, def)
    }

    @Test
    fun `cow has correct hitpoints`() {
        // Source: https://oldschool.runescape.wiki/w/Cow
        assumeTrue(dataPresent(), "monsters-complete.json absent — skipping")
        val cow = NpcDefinitions.get(2790)
        assertNotNull(cow, "Cow (id=2790) should be present in osrsbox")
        assertEquals(8, cow!!.hitpoints) { "Cow hitpoints should be 8. See: https://oldschool.runescape.wiki/w/Cow" }
    }

    @Test
    fun `cow is not aggressive`() {
        // Source: https://oldschool.runescape.wiki/w/Cow
        assumeTrue(dataPresent(), "monsters-complete.json absent — skipping")
        val cow = NpcDefinitions.get(2790)
        assertNotNull(cow)
        assertFalse(cow!!.aggressive)
    }

    @Test
    fun `npc definitions load without error`() {
        assumeTrue(dataPresent(), "monsters-complete.json absent — skipping")
        // If data is present, at least some definitions should be loaded
        // We test by checking that the cow is accessible
        assertDoesNotThrow { NpcDefinitions.get(2790) }
    }
}
