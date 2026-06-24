package persistence

import engine.TickQueueImpl
import entity.Player
import entity.Skill
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Round-trip parity tests for player save/load serialisation.
 *
 * Each test serialises a Player to a [PlayerSave] and deserialises it onto a fresh Player,
 * then asserts that the restored values match the original.
 *
 * All schema field expectations are sourced from server-persistence.md and the save schema
 * documented in the Persistence Layer Expert role definition.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlayerSaveTest {

    @BeforeAll
    fun setup() {
        // PrayerDefs required for PrayerSet construction inside Player.
        // Uses default path: data/prayers/standard.yaml (resolved from repo root).
        prayer.PrayerDefs.init()
    }

    private fun makePlayer(name: String = "testuser"): Player =
        Player(username = name, tickQueue = TickQueueImpl())

    // -------------------------------------------------------------------------
    // Schema version
    // Source: server-persistence.md — Save Format Versioning
    // -------------------------------------------------------------------------

    @Test
    fun `schema version is 1`() {
        // Current schema version must match CURRENT_VERSION = 1.
        // Source: server-persistence.md — "const val CURRENT_VERSION = 1"
        val save = PlayerSerializer.serialize(makePlayer())
        assertEquals(1, save.schemaVersion,
            "schemaVersion must equal PlayerSave.CURRENT_VERSION (1). " +
                "Source: server-persistence.md — Save Format Versioning")
    }

    // -------------------------------------------------------------------------
    // Position
    // Source: https://oldschool.runescape.wiki/w/Chunk (tile coordinates)
    // -------------------------------------------------------------------------

    @Test
    fun `position round-trips`() {
        // Lumbridge spawn coordinates: x=3222, y=3218, plane=0
        // Source: https://oldschool.runescape.wiki/w/Lumbridge
        val p = makePlayer()
        p.x = 3222; p.y = 3218; p.plane = 0
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        assertEquals(3222, p2.x,
            "x=3222 must survive round-trip. Source: https://oldschool.runescape.wiki/w/Lumbridge")
        assertEquals(3218, p2.y,
            "y=3218 must survive round-trip. Source: https://oldschool.runescape.wiki/w/Lumbridge")
        assertEquals(0, p2.plane,
            "plane=0 (ground floor) must survive round-trip. Source: https://oldschool.runescape.wiki/w/Chunk")
    }

    @Test
    fun `non-zero plane survives round-trip`() {
        // plane=2 is used by upper-floor areas such as the Lumbridge Castle 2nd floor.
        // Source: https://oldschool.runescape.wiki/w/Chunk
        val p = makePlayer()
        p.plane = 2
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        assertEquals(2, p2.plane,
            "plane=2 must survive round-trip. Source: https://oldschool.runescape.wiki/w/Chunk")
    }

    // -------------------------------------------------------------------------
    // Skill XP
    // Source: https://oldschool.runescape.wiki/w/Experience
    // -------------------------------------------------------------------------

    @Test
    fun `skill XP round-trips`() {
        // Yew tree grants 175.0 XP per log.
        // Source: https://oldschool.runescape.wiki/w/Woodcutting#Experience
        val p = makePlayer()
        p.skills.addXp(Skill.WOODCUTTING, 1234.5)
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        assertEquals(1234.5, p2.skills.getXp(Skill.WOODCUTTING), 0.001,
            "Woodcutting XP 1234.5 must survive round-trip. " +
                "Source: https://oldschool.runescape.wiki/w/Experience")
    }

    @Test
    fun `all 23 skills preserved in save`() {
        // OSRS has 23 skills. All must be serialised and restored.
        // Source: https://oldschool.runescape.wiki/w/Skills
        val p = makePlayer()
        Skill.entries.forEachIndexed { i, skill -> p.skills.addXp(skill, (i + 1) * 100.0) }
        val save = PlayerSerializer.serialize(p)
        assertEquals(23, save.skillXp.size,
            "All 23 OSRS skills must be included in skillXp map. " +
                "Source: https://oldschool.runescape.wiki/w/Skills")
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        Skill.entries.forEachIndexed { i, skill ->
            assertEquals((i + 1) * 100.0, p2.skills.getXp(skill), 0.001,
                "${skill.name} XP must survive round-trip. " +
                    "Source: https://oldschool.runescape.wiki/w/Experience")
        }
    }

    @Test
    fun `max XP 200_000_000 is preserved`() {
        // Maximum XP per skill in OSRS is 200,000,000.
        // Source: https://oldschool.runescape.wiki/w/Experience
        val p = makePlayer()
        p.skills.addXp(Skill.ATTACK, 200_000_000.0)
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        assertEquals(200_000_000.0, p2.skills.getXp(Skill.ATTACK), 0.001,
            "200M XP cap must survive round-trip. " +
                "Source: https://oldschool.runescape.wiki/w/Experience")
    }

    // -------------------------------------------------------------------------
    // Prayer points
    // Source: https://oldschool.runescape.wiki/w/Prayer#Points
    // -------------------------------------------------------------------------

    @Test
    fun `prayer points round-trip when prayer level is sufficient`() {
        // A player at Prayer level 43 (Thick Skin..Protect from Magic range) can hold
        // 43 prayer points. We restore the saved prayer points via restorePoints().
        // Source: https://oldschool.runescape.wiki/w/Prayer#Points
        val p = makePlayer()
        // XP for level 43 Prayer. Level 43 = 47,065 XP.
        // Source: https://oldschool.runescape.wiki/w/Experience
        p.skills.addXp(Skill.PRAYER, 47_065.0)
        p.prayer.fillPoints()           // fill to Prayer level
        val expectedPoints = p.prayer.prayerPoints  // should equal Prayer level (43)
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        p2.skills.addXp(Skill.PRAYER, 47_065.0)    // must restore Prayer XP first
        PlayerSerializer.deserialize(save, p2)
        // deserialize also restores Prayer XP (already set above), then calls restorePoints
        // which clamps to Prayer level; result must equal the saved value.
        assertEquals(expectedPoints, p2.prayer.prayerPoints,
            "Prayer points $expectedPoints must survive round-trip at matching Prayer level. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Points")
    }

    // -------------------------------------------------------------------------
    // Inventory
    // Source: https://oldschool.runescape.wiki/w/Inventory
    // -------------------------------------------------------------------------

    @Test
    fun `inventory items round-trip by slot`() {
        // Abyssal whip (item ID 4151) in slot 0; coins (item ID 995) in slot 1.
        // Source: https://oldschool.runescape.wiki/w/Inventory
        val p = makePlayer()
        p.inventory.setSlot(0, 4151, 1)       // abyssal whip
        p.inventory.setSlot(1, 995, 50_000)   // coins
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        val whip = p2.inventory.getSlot(0)
        val coins = p2.inventory.getSlot(1)
        assertEquals(4151, whip?.itemId,
            "Abyssal whip (4151) must restore to slot 0. " +
                "Source: https://oldschool.runescape.wiki/w/Inventory")
        assertEquals(1, whip?.quantity,
            "Abyssal whip quantity=1 must survive round-trip. " +
                "Source: https://oldschool.runescape.wiki/w/Inventory")
        assertEquals(995, coins?.itemId,
            "Coins (995) must restore to slot 1. " +
                "Source: https://oldschool.runescape.wiki/w/Inventory")
        assertEquals(50_000, coins?.quantity,
            "Coins quantity=50000 must survive round-trip. " +
                "Source: https://oldschool.runescape.wiki/w/Inventory")
    }

    @Test
    fun `empty inventory slots serialise as null`() {
        // An empty inventory must produce 28 null entries in the save.
        // Source: https://oldschool.runescape.wiki/w/Inventory
        val p = makePlayer()
        val save = PlayerSerializer.serialize(p)
        assertEquals(28, save.inventory.size,
            "Inventory must serialise to exactly 28 slots. " +
                "Source: https://oldschool.runescape.wiki/w/Inventory")
        save.inventory.forEachIndexed { i, slot ->
            assertNull(slot, "Empty inventory slot $i must serialise as null. " +
                "Source: https://oldschool.runescape.wiki/w/Inventory")
        }
    }

    @Test
    fun `partial inventory preserves slot positions`() {
        // Items placed into specific slots must restore to those exact slots.
        // OSRS preserves item order in the inventory (not rearranged on load).
        // Source: https://oldschool.runescape.wiki/w/Inventory
        val p = makePlayer()
        p.inventory.setSlot(5, 1515, 100)    // yew logs in slot 5
        p.inventory.setSlot(27, 886, 50)     // mithril arrows in last slot
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        assertNull(p2.inventory.getSlot(0),
            "Slot 0 must remain empty. Source: https://oldschool.runescape.wiki/w/Inventory")
        val yewLogs = p2.inventory.getSlot(5)
        assertEquals(1515, yewLogs?.itemId,
            "Yew logs (1515) must restore to slot 5. " +
                "Source: https://oldschool.runescape.wiki/w/Yew_logs")
        assertEquals(100, yewLogs?.quantity,
            "Yew logs quantity=100 must survive round-trip. " +
                "Source: https://oldschool.runescape.wiki/w/Yew_logs")
        val arrows = p2.inventory.getSlot(27)
        assertEquals(886, arrows?.itemId,
            "Mithril arrows (886) must restore to slot 27. " +
                "Source: https://oldschool.runescape.wiki/w/Mithril_arrow")
    }

    // -------------------------------------------------------------------------
    // Equipment
    // Source: https://oldschool.runescape.wiki/w/Equipment
    // -------------------------------------------------------------------------

    @Test
    fun `equipment round-trips`() {
        // Torva full helm (26382) in HEAD slot (0); Abyssal whip (4151) in WEAPON slot (3).
        // Source: https://oldschool.runescape.wiki/w/Equipment
        val p = makePlayer()
        p.equipment[0] = 26382    // Torva full helm
        p.equipment[3] = 4151     // Abyssal whip
        val save = PlayerSerializer.serialize(p)
        val p2 = makePlayer()
        PlayerSerializer.deserialize(save, p2)
        assertEquals(26382, p2.equipment[0],
            "Torva full helm (26382) must restore to equipment slot 0 (HEAD). " +
                "Source: https://oldschool.runescape.wiki/w/Equipment")
        assertEquals(4151, p2.equipment[3],
            "Abyssal whip (4151) must restore to equipment slot 3 (WEAPON). " +
                "Source: https://oldschool.runescape.wiki/w/Equipment")
    }

    @Test
    fun `empty equipment slots restore as minus 1`() {
        // Player.equipment initialises to -1 per slot. Unequipped slots must stay -1.
        // Source: https://oldschool.runescape.wiki/w/Equipment
        val p = makePlayer()
        val save = PlayerSerializer.serialize(p)
        assertEquals(14, save.equipment.size,
            "Equipment must serialise to exactly 14 slots. " +
                "Source: https://oldschool.runescape.wiki/w/Equipment")
        save.equipment.forEachIndexed { i, id ->
            assertEquals(-1, id,
                "Empty equipment slot $i must serialise as -1. " +
                    "Source: https://oldschool.runescape.wiki/w/Equipment")
        }
    }

    // -------------------------------------------------------------------------
    // Username
    // -------------------------------------------------------------------------

    @Test
    fun `username preserved in save`() {
        // The username in the save must exactly match the Player.username field.
        // Source: server-persistence.md — Save Format Versioning
        val p = makePlayer("ironman_joe")
        val save = PlayerSerializer.serialize(p)
        assertEquals("ironman_joe", save.username,
            "username 'ironman_joe' must be preserved in the save. " +
                "Source: server-persistence.md — Save Format Versioning")
    }
}
