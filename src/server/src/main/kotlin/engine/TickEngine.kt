package engine

import io.netty.util.HashedWheelTimer
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 600ms server tick loop using Netty HashedWheelTimer.
 *
 * Entity update order matches OSRS exactly per server-tick.md:
 *   1. Player queued actions
 *   2. NPC movement & AI              (stub — wired by npc-behavior-simulator)
 *   3. Player update blocks            (stub — wired by protocol-packet-engine)
 *   4. NPC update blocks               (stub — wired by protocol-packet-engine)
 *   5. Ground item updates             (stub — wired by world ground-item system)
 *   6. Object/scenery updates          (stub — wired by world-region-loader)
 *
 * Source: https://oldschool.runescape.wiki/w/Server#The_tick_system
 */
class TickEngine(
    private val tickDurationMs: Long = TICK_MS,
) {
    companion object {
        const val TICK_MS = 600L
        const val TICK_BUDGET_MS = 580L
        private val log = LoggerFactory.getLogger(TickEngine::class.java)
    }

    // 100ms wheel resolution — provides smooth scheduling with headroom inside a 600ms tick
    private val timer = HashedWheelTimer(100, TimeUnit.MILLISECONDS, 16)
    private val queues = CopyOnWriteArrayList<TickQueueImpl>()
    private val tickNum = AtomicLong(0L)

    @Volatile
    var running = false
        private set

    val currentTick: Long get() = tickNum.get()

    fun start() {
        check(!running) { "TickEngine already running" }
        running = true
        scheduleTick(tickDurationMs)
        log.info("TickEngine started (${tickDurationMs}ms per tick)")
    }

    fun stop() {
        running = false
        timer.stop()
        log.info("TickEngine stopped at tick ${tickNum.get()}")
    }

    /** Creates a new [TickQueueImpl] registered with this engine. Call on player login. */
    fun newQueue(): TickQueueImpl = TickQueueImpl().also { queues.add(it) }

    /** Unregisters a [TickQueueImpl]. Call on player logout. */
    fun removeQueue(queue: TickQueueImpl) {
        queues.remove(queue)
    }

    private fun scheduleTick(delayMs: Long) {
        if (!running) return
        val scheduleWallTime = System.currentTimeMillis()
        timer.newTimeout({ _ ->
            val tick = tickNum.incrementAndGet()
            val tickStart = System.currentTimeMillis()

            runTick(tick)

            val elapsed = System.currentTimeMillis() - tickStart
            if (elapsed > TICK_BUDGET_MS) {
                log.warn("Tick slip: tick $tick took ${elapsed}ms (budget ${TICK_BUDGET_MS}ms)")
            }

            // Schedule next tick so it fires tickDurationMs after this one started,
            // absorbing processing time to maintain approximate 600ms cadence.
            val nextDelay = (tickDurationMs - (System.currentTimeMillis() - scheduleWallTime - delayMs + elapsed))
                .coerceAtLeast(0L)
            scheduleTick(nextDelay)
        }, delayMs, TimeUnit.MILLISECONDS)
    }

    private fun runTick(tick: Long) {
        // 1. Player queued actions
        queues.forEach { q -> q.tick(tick) }
        // 2-6: stubs for future phases (see class-level kdoc)
    }
}
