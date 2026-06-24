package prayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Parity tests for prayer drain mechanics.
 * All expected values are derived from the OSRS wiki drain formula.
 *
 * Drain formula:
 *   threshold = 600 * (1 + floor(prayerBonus / 30))
 *   accumulator += sum(active drain_effects) each tick
 *   when accumulator >= threshold: lose 1 prayer point
 *
 * Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PrayerDrainParityTest {

    @BeforeAll
    fun setup() {
        PrayerDefs.init()
    }

    /** Creates a PrayerSet at the given Prayer level with full points. */
    private fun prayerSetAtLevel(level: Int): PrayerSet {
        val ps = PrayerSet { level }
        ps.fillPoints()
        return ps
    }

    // -------------------------------------------------------------------------
    // Test 1: Ultimate Strength (drain=12) at bonus 0
    // threshold = 600 * (1 + 0/30) = 600
    // ticks per point = 600 / 12 = 50
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `ultimate strength drain effect 12 at bonus 0 loses 1 point after 50 ticks`() {
        val ps = prayerSetAtLevel(99)
        val initialPoints = ps.prayerPoints
        ps.activate(Prayer.ULTIMATE_STRENGTH)
        assertTrue(ps.activePrayers.contains(Prayer.ULTIMATE_STRENGTH),
            "Ultimate Strength should be active. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // 49 ticks: accumulator = 49 * 12 = 588, threshold = 600 — no drain yet
        repeat(49) { ps.tick(0) }
        assertEquals(initialPoints, ps.prayerPoints,
            "After 49 ticks with Ultimate Strength (drain=12, threshold=600): " +
                "accumulator=588 < 600, no drain yet. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // 50th tick: accumulator = 50 * 12 = 600 >= threshold=600, drain 1 point
        ps.tick(0)
        assertEquals(initialPoints - 1, ps.prayerPoints,
            "After 50 ticks with Ultimate Strength (drain=12, threshold=600): " +
                "exactly 1 prayer point drained. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }

    // -------------------------------------------------------------------------
    // Test 2: Ultimate Strength at bonus 30
    // threshold = 600 * (1 + 30/30) = 600 * 2 = 1200
    // ticks per point = 1200 / 12 = 100
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `ultimate strength at prayer bonus 30 takes 100 ticks per point`() {
        val ps = prayerSetAtLevel(99)
        val initialPoints = ps.prayerPoints
        ps.activate(Prayer.ULTIMATE_STRENGTH)

        // 99 ticks: accumulator = 99 * 12 = 1188 < 1200 — no drain yet
        repeat(99) { ps.tick(30) }
        assertEquals(initialPoints, ps.prayerPoints,
            "After 99 ticks with Ultimate Strength and prayer bonus 30 " +
                "(threshold=1200): accumulator=1188, no drain. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // 100th tick: accumulator = 1200 >= 1200, drain 1 point
        ps.tick(30)
        assertEquals(initialPoints - 1, ps.prayerPoints,
            "After 100 ticks with prayer bonus 30 (threshold=1200): 1 point drained. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }

    // -------------------------------------------------------------------------
    // Test 3: Piety (drain=24) at bonus 0
    // threshold = 600, ticks per point = 600 / 24 = 25
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `piety drain effect 24 at bonus 0 loses 1 point after 25 ticks`() {
        val ps = prayerSetAtLevel(99)
        val initialPoints = ps.prayerPoints
        ps.activate(Prayer.PIETY)

        // 24 ticks: accumulator = 24 * 24 = 576 < 600 — no drain yet
        repeat(24) { ps.tick(0) }
        assertEquals(initialPoints, ps.prayerPoints,
            "After 24 ticks with Piety (drain=24, threshold=600): " +
                "accumulator=576, no drain. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // 25th tick: accumulator = 600 >= 600, drain 1 point
        ps.tick(0)
        assertEquals(initialPoints - 1, ps.prayerPoints,
            "After 25 ticks with Piety (drain=24, threshold=600): " +
                "1 prayer point drained. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }

    // -------------------------------------------------------------------------
    // Test 4: Multiple prayers — Piety (24) + Protect from Melee (12) = total 36
    // threshold = 600, ticks to drain 1 point = ceil(600 / 36) = 17 ticks
    // Because: 16 * 36 = 576 < 600; 17 * 36 = 612 >= 600
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `piety plus protect from melee combined drain 36 loses 1 point after 17 ticks`() {
        val ps = prayerSetAtLevel(99)
        val initialPoints = ps.prayerPoints
        ps.activate(Prayer.PIETY)
        ps.activate(Prayer.PROTECT_FROM_MELEE)
        assertTrue(ps.activePrayers.contains(Prayer.PIETY),
            "Piety should be active. Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
        assertTrue(ps.activePrayers.contains(Prayer.PROTECT_FROM_MELEE),
            "Protect from Melee should be active alongside Piety. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // 16 ticks: accumulator = 16 * 36 = 576 < 600 — no drain yet
        repeat(16) { ps.tick(0) }
        assertEquals(initialPoints, ps.prayerPoints,
            "After 16 ticks with Piety+Protect from Melee (drain=36, threshold=600): " +
                "accumulator=576, no drain. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // 17th tick: accumulator = 612 >= 600, drain 1 point, accumulator = 12
        ps.tick(0)
        assertEquals(initialPoints - 1, ps.prayerPoints,
            "After 17 ticks with combined drain 36 (threshold=600): 1 point drained. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }

    // -------------------------------------------------------------------------
    // Test 5: No active prayers — no drain after 100 ticks
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `no active prayers causes no drain after 100 ticks`() {
        val ps = prayerSetAtLevel(99)
        val initialPoints = ps.prayerPoints
        assertTrue(ps.activePrayers.isEmpty(),
            "No prayers should be active initially. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        repeat(100) { ps.tick(0) }
        assertEquals(initialPoints, ps.prayerPoints,
            "With no prayers active, prayer points should not drain after 100 ticks. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }

    // -------------------------------------------------------------------------
    // Test 6: Points clamp to 0 and prayers deactivate when exhausted
    // A level-99 player with exactly 1 prayer point activates Piety (level 70 req).
    // Piety drain=24, threshold=600 → 25 ticks to exhaust 1 point.
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `prayers deactivate when points reach 0`() {
        // Level 99 so Piety (level 70 required) can activate; restore only 1 point
        val ps = PrayerSet { 99 }
        ps.restorePoints(1)
        assertEquals(1, ps.prayerPoints,
            "restorePoints(1) at level 99 should give exactly 1 prayer point. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
        val activated = ps.activate(Prayer.PIETY)
        assertTrue(activated,
            "Piety should activate when level is sufficient and points > 0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
        assertTrue(ps.activePrayers.contains(Prayer.PIETY),
            "Piety should be in active prayer set. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // With 1 prayer point and Piety drain=24, threshold=600, need 25 ticks to exhaust.
        // After 25 ticks prayerPoints = 0, prayers should deactivate.
        repeat(25) { ps.tick(0) }
        assertEquals(0, ps.prayerPoints,
            "After 25 ticks with Piety (drain=24, threshold=600) and 1 initial point: " +
                "prayer points should reach 0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
        assertTrue(ps.activePrayers.isEmpty(),
            "All prayers must deactivate when prayer points reach 0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }

    // -------------------------------------------------------------------------
    // Test 7: Prayer bonus 60 — threshold = 600 * (1 + 60/30) = 1800
    // ticks per point for Piety (drain=24) = 1800/24 = 75
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `prayer bonus 60 triples drain threshold to 1800 for piety`() {
        val ps = prayerSetAtLevel(99)
        val initialPoints = ps.prayerPoints
        ps.activate(Prayer.PIETY)

        // 74 ticks: accumulator = 74 * 24 = 1776 < 1800 — no drain
        repeat(74) { ps.tick(60) }
        assertEquals(initialPoints, ps.prayerPoints,
            "After 74 ticks with Piety and prayer bonus 60 (threshold=1800): " +
                "accumulator=1776, no drain. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")

        // 75th tick: accumulator = 1800 >= 1800, drain 1 point
        ps.tick(60)
        assertEquals(initialPoints - 1, ps.prayerPoints,
            "After 75 ticks with prayer bonus 60 (threshold=1800): 1 point drained. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }

    // -------------------------------------------------------------------------
    // Test 8: activate() returns false when prayer points are 0
    // Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
    // -------------------------------------------------------------------------
    @Test fun `activate returns false when no prayer points`() {
        val ps = PrayerSet { 99 }
        // prayerPoints is 0 (not filled)
        val result = ps.activate(Prayer.THICK_SKIN)
        assertFalse(result,
            "activate() should return false when prayer points = 0. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
        assertTrue(ps.activePrayers.isEmpty(),
            "No prayers should activate with 0 prayer points. " +
                "Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate")
    }
}
