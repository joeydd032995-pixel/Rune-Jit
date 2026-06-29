package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import skills.construction.ConstructionConfig
import skills.construction.ConstructionLoader
import java.nio.file.Path

/**
 * Parity tests for Construction skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Construction
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConstructionParityTest {

    private lateinit var config: ConstructionConfig

    @BeforeAll
    fun loadData() {
        config = ConstructionLoader.load(Path.of("data/skills/construction.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * 2 ticks per furniture build (1.2 seconds).
     * Source: https://oldschool.runescape.wiki/w/Construction#Building_furniture
     */
    @Test fun `tick rate is 2 ticks per build`() {
        assertEquals(2, config.meta.ticksPerBuild,
            "ticks_per_build must be 2. See: https://oldschool.runescape.wiki/w/Construction#Building_furniture")
    }

    /**
     * Saw item ID is 8794.
     * Source: https://oldschool.runescape.wiki/w/Saw
     */
    @Test fun `saw item id is 8794`() {
        assertEquals(8794, config.meta.toolSawItemId,
            "tool_saw_item_id must be 8794. See: https://oldschool.runescape.wiki/w/Saw")
    }

    /**
     * Hammer item ID is 2347.
     * Source: https://oldschool.runescape.wiki/w/Hammer
     */
    @Test fun `hammer item id is 2347`() {
        assertEquals(2347, config.meta.toolHammerItemId,
            "tool_hammer_item_id must be 2347. See: https://oldschool.runescape.wiki/w/Hammer")
    }

    // -------------------------------------------------------------------------
    // Plank item IDs and XP — Source: https://oldschool.runescape.wiki/w/Plank#Types
    // -------------------------------------------------------------------------

    @Test fun `plank item id is 960`() {
        assertEquals(960, config.planks["PLANK"]!!.itemId,
            "Regular plank item ID must be 960. See: https://oldschool.runescape.wiki/w/Plank")
    }

    @Test fun `oak plank item id is 8778`() {
        assertEquals(8778, config.planks["OAK_PLANK"]!!.itemId,
            "Oak plank item ID must be 8778. See: https://oldschool.runescape.wiki/w/Oak_plank")
    }

    @Test fun `teak plank item id is 8780`() {
        assertEquals(8780, config.planks["TEAK_PLANK"]!!.itemId,
            "Teak plank item ID must be 8780. See: https://oldschool.runescape.wiki/w/Teak_plank")
    }

    @Test fun `mahogany plank item id is 8782`() {
        assertEquals(8782, config.planks["MAHOGANY_PLANK"]!!.itemId,
            "Mahogany plank item ID must be 8782. See: https://oldschool.runescape.wiki/w/Mahogany_plank")
    }

    @Test fun `plank xp per plank is 29_0`() {
        assertEquals(29.0, config.planks["PLANK"]!!.xpPerPlank,
            "Regular plank xp_per_plank must be 29.0. See: https://oldschool.runescape.wiki/w/Plank")
    }

    @Test fun `oak plank xp per plank is 60_0`() {
        assertEquals(60.0, config.planks["OAK_PLANK"]!!.xpPerPlank,
            "Oak plank xp_per_plank must be 60.0. See: https://oldschool.runescape.wiki/w/Oak_plank")
    }

    @Test fun `teak plank xp per plank is 90_0`() {
        assertEquals(90.0, config.planks["TEAK_PLANK"]!!.xpPerPlank,
            "Teak plank xp_per_plank must be 90.0. See: https://oldschool.runescape.wiki/w/Teak_plank")
    }

    @Test fun `mahogany plank xp per plank is 140_0`() {
        assertEquals(140.0, config.planks["MAHOGANY_PLANK"]!!.xpPerPlank,
            "Mahogany plank xp_per_plank must be 140.0. See: https://oldschool.runescape.wiki/w/Mahogany_plank")
    }

    // -------------------------------------------------------------------------
    // Room level requirements — Source: https://oldschool.runescape.wiki/w/Construction#Rooms
    // -------------------------------------------------------------------------

    @Test fun `garden requires level 1`() = assertRoomLevel("GARDEN", 1)
    @Test fun `parlour requires level 1`() = assertRoomLevel("PARLOUR", 1)
    @Test fun `kitchen requires level 5`() = assertRoomLevel("KITCHEN", 5)
    @Test fun `dining room requires level 10`() = assertRoomLevel("DINING_ROOM", 10)
    @Test fun `workshop requires level 15`() = assertRoomLevel("WORKSHOP", 15)
    @Test fun `bedroom requires level 20`() = assertRoomLevel("BEDROOM", 20)
    @Test fun `skill hall requires level 25`() = assertRoomLevel("SKILL_HALL", 25)
    @Test fun `games room requires level 30`() = assertRoomLevel("GAMES_ROOM", 30)
    @Test fun `quest hall requires level 35`() = assertRoomLevel("QUEST_HALL", 35)
    @Test fun `study requires level 40`() = assertRoomLevel("STUDY", 40)
    @Test fun `chapel requires level 45`() = assertRoomLevel("CHAPEL", 45)
    @Test fun `portal chamber requires level 50`() = assertRoomLevel("PORTAL_CHAMBER", 50)
    @Test fun `throne room requires level 60`() = assertRoomLevel("THRONE_ROOM", 60)
    @Test fun `dungeon corridor requires level 70`() = assertRoomLevel("DUNGEON_CORRIDOR", 70)
    @Test fun `achievement gallery requires level 80`() = assertRoomLevel("ACHIEVEMENT_GALLERY", 80)

    // -------------------------------------------------------------------------
    // Room build costs — Source: https://oldschool.runescape.wiki/w/Construction#Rooms
    // -------------------------------------------------------------------------

    @Test fun `garden build cost is 1000`() {
        assertEquals(1000, config.rooms["GARDEN"]!!.buildCost,
            "Garden build_cost must be 1,000 gp. See: https://oldschool.runescape.wiki/w/Garden")
    }

    @Test fun `portal chamber build cost is 100000`() {
        assertEquals(100000, config.rooms["PORTAL_CHAMBER"]!!.buildCost,
            "Portal Chamber build_cost must be 100,000 gp. See: https://oldschool.runescape.wiki/w/Portal_Chamber")
    }

    @Test fun `achievement gallery build cost is 250000`() {
        assertEquals(250000, config.rooms["ACHIEVEMENT_GALLERY"]!!.buildCost,
            "Achievement Gallery build_cost must be 250,000 gp. See: https://oldschool.runescape.wiki/w/Achievement_Gallery")
    }

    /**
     * Removal refund is 50% of build cost.
     * Source: https://oldschool.runescape.wiki/w/Construction#Removing_rooms
     */
    @Test fun `garden removal refund is 50 percent of build cost`() {
        val room = config.rooms["GARDEN"]!!
        assertEquals(room.buildCost / 2, room.removalRefund,
            "Garden removal_refund must be 50%% of build_cost. See: https://oldschool.runescape.wiki/w/Construction#Removing_rooms")
    }

    @Test fun `throne room removal refund is 50 percent of build cost`() {
        val room = config.rooms["THRONE_ROOM"]!!
        assertEquals(room.buildCost / 2, room.removalRefund,
            "Throne Room removal_refund must be 50%% of build_cost. See: https://oldschool.runescape.wiki/w/Construction#Removing_rooms")
    }

    // -------------------------------------------------------------------------
    // Furniture XP — Source: https://oldschool.runescape.wiki/w/Construction#Furniture_tables
    // -------------------------------------------------------------------------

    @Test fun `crude wooden chair xp is 58_0`() = assertFurnitureXp("CRUDE_WOODEN_CHAIR", 58.0,
        "https://oldschool.runescape.wiki/w/Crude_wooden_chair")

    @Test fun `wooden chair xp is 87_5`() = assertFurnitureXp("WOODEN_CHAIR", 87.5,
        "https://oldschool.runescape.wiki/w/Wooden_chair")

    @Test fun `rocking chair xp is 87_5`() = assertFurnitureXp("ROCKING_CHAIR", 87.5,
        "https://oldschool.runescape.wiki/w/Rocking_chair")

    @Test fun `oak chair xp is 120_0`() = assertFurnitureXp("OAK_CHAIR", 120.0,
        "https://oldschool.runescape.wiki/w/Oak_chair")

    @Test fun `teak armchair xp is 180_0`() = assertFurnitureXp("TEAK_ARMCHAIR", 180.0,
        "https://oldschool.runescape.wiki/w/Teak_armchair")

    @Test fun `mahogany armchair xp is 280_0`() = assertFurnitureXp("MAHOGANY_ARMCHAIR", 280.0,
        "https://oldschool.runescape.wiki/w/Mahogany_armchair")

    @Test fun `oak dining table xp is 240_0`() = assertFurnitureXp("OAK_DINING_TABLE", 240.0,
        "https://oldschool.runescape.wiki/w/Oak_dining_table")

    @Test fun `teak dining table xp is 360_0`() = assertFurnitureXp("TEAK_DINING_TABLE", 360.0,
        "https://oldschool.runescape.wiki/w/Teak_dining_table")

    @Test fun `mahogany dining table xp is 840_0`() = assertFurnitureXp("MAHOGANY_DINING_TABLE", 840.0,
        "https://oldschool.runescape.wiki/w/Mahogany_dining_table")

    @Test fun `wooden bookcase xp is 115_0`() = assertFurnitureXp("WOODEN_BOOKCASE", 115.0,
        "https://oldschool.runescape.wiki/w/Wooden_bookcase")

    @Test fun `oak bookcase xp is 180_0`() = assertFurnitureXp("OAK_BOOKCASE", 180.0,
        "https://oldschool.runescape.wiki/w/Oak_bookcase")

    @Test fun `mahogany bookcase xp is 420_0`() = assertFurnitureXp("MAHOGANY_BOOKCASE", 420.0,
        "https://oldschool.runescape.wiki/w/Mahogany_bookcase")

    @Test fun `oak workbench xp is 240_0`() = assertFurnitureXp("OAK_WORKBENCH", 240.0,
        "https://oldschool.runescape.wiki/w/Oak_workbench")

    // -------------------------------------------------------------------------
    // Furniture level requirements
    // -------------------------------------------------------------------------

    @Test fun `crude wooden chair requires level 1`() = assertFurnitureLevel("CRUDE_WOODEN_CHAIR", 1)
    @Test fun `wooden chair requires level 8`() = assertFurnitureLevel("WOODEN_CHAIR", 8)
    @Test fun `rocking chair requires level 14`() = assertFurnitureLevel("ROCKING_CHAIR", 14)
    @Test fun `oak chair requires level 19`() = assertFurnitureLevel("OAK_CHAIR", 19)
    @Test fun `oak dining table requires level 22`() = assertFurnitureLevel("OAK_DINING_TABLE", 22)
    @Test fun `oak workbench requires level 33`() = assertFurnitureLevel("OAK_WORKBENCH", 33)
    @Test fun `teak armchair requires level 35`() = assertFurnitureLevel("TEAK_ARMCHAIR", 35)
    @Test fun `teak dining table requires level 38`() = assertFurnitureLevel("TEAK_DINING_TABLE", 38)
    @Test fun `oak bookcase requires level 40`() = assertFurnitureLevel("OAK_BOOKCASE", 40)
    @Test fun `mahogany armchair requires level 47`() = assertFurnitureLevel("MAHOGANY_ARMCHAIR", 47)
    @Test fun `mahogany dining table requires level 52`() = assertFurnitureLevel("MAHOGANY_DINING_TABLE", 52)
    @Test fun `mahogany bookcase requires level 80`() = assertFurnitureLevel("MAHOGANY_BOOKCASE", 80)

    // -------------------------------------------------------------------------
    // Furniture materials — Source: https://oldschool.runescape.wiki/w/Construction#Furniture_tables
    // -------------------------------------------------------------------------

    @Test fun `crude wooden chair requires 2 planks`() {
        val mats = config.furniture["CRUDE_WOODEN_CHAIR"]!!.materials
        assertTrue(mats.any { it.itemId == 960 && it.quantity == 2 },
            "Crude wooden chair must require 2 planks (id 960). See: https://oldschool.runescape.wiki/w/Crude_wooden_chair")
    }

    @Test fun `oak chair requires 2 oak planks`() {
        val mats = config.furniture["OAK_CHAIR"]!!.materials
        assertTrue(mats.any { it.itemId == 8778 && it.quantity == 2 },
            "Oak chair must require 2 oak planks (id 8778). See: https://oldschool.runescape.wiki/w/Oak_chair")
    }

    @Test fun `mahogany dining table requires 6 mahogany planks`() {
        val mats = config.furniture["MAHOGANY_DINING_TABLE"]!!.materials
        assertTrue(mats.any { it.itemId == 8782 && it.quantity == 6 },
            "Mahogany dining table must require 6 mahogany planks (id 8782). See: https://oldschool.runescape.wiki/w/Mahogany_dining_table")
    }

    @Test fun `mahogany bookcase requires 3 mahogany planks`() {
        val mats = config.furniture["MAHOGANY_BOOKCASE"]!!.materials
        assertTrue(mats.any { it.itemId == 8782 && it.quantity == 3 },
            "Mahogany bookcase must require 3 mahogany planks (id 8782). See: https://oldschool.runescape.wiki/w/Mahogany_bookcase")
    }

    // -------------------------------------------------------------------------
    // Servant data — Source: https://oldschool.runescape.wiki/w/Servant#Servants
    // -------------------------------------------------------------------------

    @Test fun `servant requires level 20`() = assertServantLevel("SERVANT", 20)
    @Test fun `cook requires level 30`() = assertServantLevel("COOK", 30)
    @Test fun `gardener requires level 50`() = assertServantLevel("GARDENER", 50)
    @Test fun `butler requires level 50`() = assertServantLevel("BUTLER", 50)
    @Test fun `demon butler requires level 58`() = assertServantLevel("DEMON_BUTLER", 58)

    @Test fun `demon butler travel ticks is 8`() {
        assertEquals(8, config.servants["DEMON_BUTLER"]!!.travelTicks,
            "Demon butler travel_ticks must be 8. See: https://oldschool.runescape.wiki/w/Servant#Servants")
    }

    @Test fun `demon butler max carry is 10`() {
        assertEquals(10, config.servants["DEMON_BUTLER"]!!.maxCarry,
            "Demon butler max_carry must be 10. See: https://oldschool.runescape.wiki/w/Servant#Servants")
    }

    @Test fun `demon butler wage is 10000 per 5 trips`() {
        assertEquals(10000, config.servants["DEMON_BUTLER"]!!.wagePer5Trips,
            "Demon butler wage_per_5_trips must be 10,000 gp. See: https://oldschool.runescape.wiki/w/Servant#Servants")
    }

    @Test fun `butler max carry is 10`() {
        assertEquals(10, config.servants["BUTLER"]!!.maxCarry,
            "Butler max_carry must be 10. See: https://oldschool.runescape.wiki/w/Servant#Servants")
    }

    @Test fun `servant max carry is 6`() {
        assertEquals(6, config.servants["SERVANT"]!!.maxCarry,
            "Servant max_carry must be 6. See: https://oldschool.runescape.wiki/w/Servant#Servants")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertFurnitureXp(key: String, expectedXp: Double, source: String) {
        val f = config.furniture[key] ?: error("Furniture '$key' not found in construction.yaml")
        assertEquals(expectedXp, f.xp,
            "$key XP should be $expectedXp. See: $source")
    }

    private fun assertFurnitureLevel(key: String, expectedLevel: Int) {
        val f = config.furniture[key] ?: error("Furniture '$key' not found in construction.yaml")
        assertEquals(expectedLevel, f.levelRequired,
            "$key level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Construction#Furniture_tables")
    }

    private fun assertRoomLevel(key: String, expectedLevel: Int) {
        val r = config.rooms[key] ?: error("Room '$key' not found in construction.yaml")
        assertEquals(expectedLevel, r.levelRequired,
            "$key level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Construction#Rooms")
    }

    private fun assertServantLevel(key: String, expectedLevel: Int) {
        val s = config.servants[key] ?: error("Servant '$key' not found in construction.yaml")
        assertEquals(expectedLevel, s.levelRequired,
            "$key level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Servant#Servants")
    }
}
