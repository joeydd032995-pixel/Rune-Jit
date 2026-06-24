package engine

import java.util.concurrent.ThreadLocalRandom

/**
 * Server-authoritative RNG. All hit rolls, success rolls, and drop rolls
 * use this — never accept RNG results from the client.
 * Source: server-tick.md — RNG must be server-authoritative.
 */
object OsrsRandom {
    fun nextInt(bound: Int): Int = ThreadLocalRandom.current().nextInt(bound)
}
