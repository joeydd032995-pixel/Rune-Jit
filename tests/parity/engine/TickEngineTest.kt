package engine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Parity tests for the tick engine scheduling contract.
 *
 * The OSRS server runs at 600ms per tick (one game tick = 0.6 seconds).
 * All skill actions, combat, and movement are scheduled in multiples of this tick.
 * Source: https://oldschool.runescape.wiki/w/Server#The_tick_system
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TickEngineTest {

    // -------------------------------------------------------------------------
    // TickQueueImpl scheduling contract
    // -------------------------------------------------------------------------

    /**
     * Events scheduled N ticks out must fire exactly on tick N, not before or after.
     * Source: https://oldschool.runescape.wiki/w/Server#The_tick_system
     */
    @Test fun `event fires at correct tick`() {
        val queue = TickQueueImpl()
        var fired = false
        var firedAtTick = -1L

        queue.schedule(3) { tick ->
            fired = true
            firedAtTick = tick
            false
        }

        queue.tick(1)
        assertFalse(fired, "Event must not fire on tick 1 (scheduled for tick 3)")
        queue.tick(2)
        assertFalse(fired, "Event must not fire on tick 2 (scheduled for tick 3)")
        queue.tick(3)
        assertTrue(fired, "Event must fire on tick 3")
        assertEquals(3L, firedAtTick, "Event must receive tick 3 as currentTick")
    }

    /**
     * Woodcutting fires every 4 ticks (2.4 seconds).
     * Source: https://oldschool.runescape.wiki/w/Woodcutting#Mechanics
     */
    @Test fun `4-tick woodcutting cadence`() {
        val queue = TickQueueImpl()
        val firedAtTicks = mutableListOf<Long>()

        fun reschedule(): TickEvent {
            lateinit var event: TickEvent
            event = TickEvent { tick ->
                firedAtTicks.add(tick)
                if (firedAtTicks.size < 3) queue.schedule(4, event)
                false
            }
            return event
        }

        queue.schedule(4, reschedule())
        for (t in 1L..12L) queue.tick(t)

        assertEquals(
            listOf(4L, 8L, 12L),
            firedAtTicks,
            "Woodcutting must fire at ticks 4, 8, 12 (every 4 ticks). " +
                "See: https://oldschool.runescape.wiki/w/Woodcutting#Mechanics"
        )
    }

    /**
     * Cancelling an event before its due tick must prevent it from ever firing.
     * Source: https://oldschool.runescape.wiki/w/Server#The_tick_system
     */
    @Test fun `cancel prevents event from firing`() {
        val queue = TickQueueImpl()
        var fired = false

        val event = TickEvent { _ -> fired = true; false }
        queue.schedule(5, event)
        queue.cancel(event)

        for (t in 1L..10L) queue.tick(t)
        assertFalse(fired, "Cancelled event must never fire")
    }

    /**
     * Multiple events scheduled for the same tick must all fire on that tick.
     * Source: https://oldschool.runescape.wiki/w/Server#The_tick_system
     */
    @Test fun `multiple events at same tick all fire`() {
        val queue = TickQueueImpl()
        val results = mutableListOf<Int>()

        queue.schedule(5) { _ -> results.add(1); false }
        queue.schedule(5) { _ -> results.add(2); false }
        queue.schedule(5) { _ -> results.add(3); false }

        for (t in 1L..6L) queue.tick(t)

        assertEquals(3, results.size, "All 3 events must fire on tick 5")
        assertTrue(results.containsAll(listOf(1, 2, 3)), "All 3 event IDs must appear in results")
    }

    /**
     * An event that throws must not prevent subsequent events in the same tick from running.
     * Defensive: a single broken action must not freeze the tick loop.
     */
    @Test fun `event exception does not stop subsequent events`() {
        val queue = TickQueueImpl()
        var secondFired = false

        queue.schedule(1) { _ -> throw RuntimeException("intentional test error") }
        queue.schedule(1) { _ -> secondFired = true; false }

        queue.tick(1)
        assertTrue(secondFired, "Second event must fire even if first event threw")
    }

    /**
     * An event scheduled from within process() (self-rescheduling) must fire after the
     * correct delay from the current tick, not from tick 0.
     * Source: https://oldschool.runescape.wiki/w/Server#The_tick_system
     */
    @Test fun `self-rescheduling event fires at correct future tick`() {
        val queue = TickQueueImpl()
        val firedAtTicks = mutableListOf<Long>()

        lateinit var event: TickEvent
        event = TickEvent { tick ->
            firedAtTicks.add(tick)
            if (firedAtTicks.size < 2) queue.schedule(2, event)
            false
        }
        queue.schedule(2, event)

        for (t in 1L..8L) queue.tick(t)

        assertEquals(listOf(2L, 4L), firedAtTicks,
            "Self-rescheduling event (every 2 ticks) must fire at ticks 2 and 4")
    }
}
