package parity.skilling

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import skills.firemaking.FiremakingConfig
import skills.firemaking.FiremakingLoader
import java.nio.file.Path

/**
 * Parity tests for Firemaking skill data.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Firemaking
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FiremakingParityTest {

    private lateinit var config: FiremakingConfig

    @BeforeAll
    fun loadData() {
        config = FiremakingLoader.load(Path.of("data/skills/firemaking.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta — Source: https://oldschool.runescape.wiki/w/Firemaking#Mechanics
    // -------------------------------------------------------------------------

    @Test fun `tick rate is 3 ticks per attempt`() {
        assertEquals(3, config.meta.ticksPerAttempt,
            "Firemaking ticks_per_attempt must be 3. See: https://oldschool.runescape.wiki/w/Firemaking#Mechanics")
    }

    @Test fun `tinderbox item id is 590`() {
        assertEquals(590, config.meta.tinderboxItemId,
            "Tinderbox item ID must be 590. See: https://oldschool.runescape.wiki/w/Tinderbox")
    }

    // -------------------------------------------------------------------------
    // Log XP values — Source: https://oldschool.runescape.wiki/w/Firemaking#Logs
    // -------------------------------------------------------------------------

    @Test fun `normal logs xp is 40_0`() = assertLogXp("NORMAL", 40.0,
        "https://oldschool.runescape.wiki/w/Logs")

    @Test fun `achey logs xp is 40_0`() = assertLogXp("ACHEY", 40.0,
        "https://oldschool.runescape.wiki/w/Achey_tree_logs")

    @Test fun `oak logs xp is 60_0`() = assertLogXp("OAK", 60.0,
        "https://oldschool.runescape.wiki/w/Oak_logs")

    @Test fun `willow logs xp is 90_0`() = assertLogXp("WILLOW", 90.0,
        "https://oldschool.runescape.wiki/w/Willow_logs")

    @Test fun `teak logs xp is 105_0`() = assertLogXp("TEAK", 105.0,
        "https://oldschool.runescape.wiki/w/Teak_logs")

    @Test fun `arctic pine logs xp is 125_0`() = assertLogXp("ARCTIC_PINE", 125.0,
        "https://oldschool.runescape.wiki/w/Arctic_pine_logs")

    @Test fun `maple logs xp is 135_0`() = assertLogXp("MAPLE", 135.0,
        "https://oldschool.runescape.wiki/w/Maple_logs")

    @Test fun `mahogany logs xp is 157_5`() = assertLogXp("MAHOGANY", 157.5,
        "https://oldschool.runescape.wiki/w/Mahogany_logs")

    @Test fun `yew logs xp is 202_5`() = assertLogXp("YEW", 202.5,
        "https://oldschool.runescape.wiki/w/Yew_logs")

    @Test fun `blisterwood logs xp is 96_0`() = assertLogXp("BLISTERWOOD", 96.0,
        "https://oldschool.runescape.wiki/w/Blisterwood_logs")

    @Test fun `magic logs xp is 303_8`() = assertLogXp("MAGIC", 303.8,
        "https://oldschool.runescape.wiki/w/Magic_logs")

    @Test fun `redwood logs xp is 350_0`() = assertLogXp("REDWOOD", 350.0,
        "https://oldschool.runescape.wiki/w/Redwood_logs")

    // -------------------------------------------------------------------------
    // Level requirements — Source: https://oldschool.runescape.wiki/w/Firemaking#Logs
    // -------------------------------------------------------------------------

    @Test fun `normal logs require level 1`() = assertLogLevel("NORMAL", 1)
    @Test fun `achey logs require level 1`() = assertLogLevel("ACHEY", 1)
    @Test fun `oak logs require level 15`() = assertLogLevel("OAK", 15)
    @Test fun `willow logs require level 30`() = assertLogLevel("WILLOW", 30)
    @Test fun `teak logs require level 35`() = assertLogLevel("TEAK", 35)
    @Test fun `arctic pine logs require level 42`() = assertLogLevel("ARCTIC_PINE", 42)
    @Test fun `maple logs require level 45`() = assertLogLevel("MAPLE", 45)
    @Test fun `mahogany logs require level 50`() = assertLogLevel("MAHOGANY", 50)
    @Test fun `yew logs require level 60`() = assertLogLevel("YEW", 60)
    @Test fun `blisterwood logs require level 62`() = assertLogLevel("BLISTERWOOD", 62)
    @Test fun `magic logs require level 75`() = assertLogLevel("MAGIC", 75)
    @Test fun `redwood logs require level 90`() = assertLogLevel("REDWOOD", 90)

    // -------------------------------------------------------------------------
    // Item IDs — Source: https://oldschool.runescape.wiki/w/Firemaking#Logs
    // -------------------------------------------------------------------------

    @Test fun `normal log item id is 1511`() = assertLogItemId("NORMAL", 1511)
    @Test fun `oak log item id is 1521`() = assertLogItemId("OAK", 1521)
    @Test fun `willow log item id is 1519`() = assertLogItemId("WILLOW", 1519)
    @Test fun `magic log item id is 1513`() = assertLogItemId("MAGIC", 1513)
    @Test fun `yew log item id is 1515`() = assertLogItemId("YEW", 1515)
    @Test fun `redwood log item id is 19669`() = assertLogItemId("REDWOOD", 19669)

    // -------------------------------------------------------------------------
    // byItemId reverse-lookup index
    // -------------------------------------------------------------------------

    @Test fun `byItemId lookup finds normal log by id 1511`() {
        val logDef = config.byItemId[1511]
            ?: error("byItemId[1511] must resolve to NORMAL log")
        assertEquals("NORMAL", logDef.name,
            "Item ID 1511 must map to NORMAL logs. See: https://oldschool.runescape.wiki/w/Logs")
    }

    @Test fun `byItemId lookup finds magic log by id 1513`() {
        val logDef = config.byItemId[1513]
            ?: error("byItemId[1513] must resolve to MAGIC log")
        assertEquals("MAGIC", logDef.name,
            "Item ID 1513 must map to MAGIC logs. See: https://oldschool.runescape.wiki/w/Magic_logs")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertLogXp(logName: String, expectedXp: Double, source: String) {
        val log = config.logs[logName]
            ?: error("Log '$logName' not found in firemaking.yaml")
        assertEquals(expectedXp, log.xp,
            "$logName XP should be $expectedXp. See: $source")
    }

    private fun assertLogLevel(logName: String, expectedLevel: Int) {
        val log = config.logs[logName]
            ?: error("Log '$logName' not found in firemaking.yaml")
        assertEquals(expectedLevel, log.levelRequired,
            "$logName level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Firemaking#Logs")
    }

    private fun assertLogItemId(logName: String, expectedId: Int) {
        val log = config.logs[logName]
            ?: error("Log '$logName' not found in firemaking.yaml")
        assertEquals(expectedId, log.itemId,
            "$logName item_id should be $expectedId. See: https://oldschool.runescape.wiki/w/Firemaking#Logs")
    }
}
