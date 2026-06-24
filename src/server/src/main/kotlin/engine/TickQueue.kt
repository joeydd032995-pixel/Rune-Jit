package engine

/**
 * Schedules tick-based events. Satisfied by the Netty HashedWheelTimer
 * implementation in /implement-tick-engine-core.
 * Source: server-tick.md — all scheduling via tick queue.
 */
interface TickQueue {
    fun schedule(delayTicks: Int, event: TickEvent)
    fun cancel(event: TickEvent)
}
