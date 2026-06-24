package engine

/**
 * A single unit of work scheduled on the server tick queue.
 * Returns true to reschedule for the next interval, false to cancel.
 * Source: server-tick.md — all game actions scheduled via tick queue, no Thread.sleep.
 */
fun interface TickEvent {
    fun process(currentTick: Long): Boolean
}
