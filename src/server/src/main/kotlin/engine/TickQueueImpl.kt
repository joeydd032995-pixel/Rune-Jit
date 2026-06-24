package engine

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class TickQueueImpl : TickQueue {

    private val buckets = ConcurrentHashMap<Long, ConcurrentLinkedQueue<TickEvent>>()

    /**
     * The tick number at which this queue currently sits.
     * Updated by [tick] before processing events, so that any [schedule] call
     * made from inside an event handler sees the correct base tick.
     */
    @Volatile internal var currentTick = 0L

    override fun schedule(delayTicks: Int, event: TickEvent) {
        require(delayTicks > 0) { "delayTicks must be > 0, got $delayTicks" }
        buckets.computeIfAbsent(currentTick + delayTicks) { ConcurrentLinkedQueue() }.add(event)
    }

    override fun cancel(event: TickEvent) {
        buckets.values.forEach { it.remove(event) }
    }

    /**
     * Advances the current tick to [nextTick] and processes all events scheduled for it.
     * Called exclusively by [TickEngine] — not part of the public [TickQueue] contract.
     */
    internal fun tick(nextTick: Long) {
        currentTick = nextTick
        val due = buckets.remove(nextTick) ?: return
        for (event in due) {
            try {
                event.process(nextTick)
            } catch (e: Exception) {
                log.error("TickEvent threw on tick $nextTick", e)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TickQueueImpl::class.java)
    }
}
