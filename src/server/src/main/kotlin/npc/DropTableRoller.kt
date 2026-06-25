package npc

import engine.OsrsRandom
import kotlin.math.floor

/**
 * Rolls an NPC's drop table using server-authoritative RNG.
 * Formula: roll = nextInt(floor(1 / rarity)); item drops if roll == 0.
 * Source: https://oldschool.runescape.wiki/w/Drop_rate
 */
object DropTableRoller {

    fun roll(drops: List<DropEntry>): List<Pair<Int, Int>> {
        val results = mutableListOf<Pair<Int, Int>>()
        for (entry in drops) {
            repeat(entry.rolls) {
                if (shouldDrop(entry.rarity)) {
                    val qty = if (entry.minQty == entry.maxQty) entry.minQty
                              else entry.minQty + OsrsRandom.nextInt(entry.maxQty - entry.minQty + 1)
                    results.add(entry.itemId to qty)
                }
            }
        }
        return results
    }

    internal fun shouldDrop(rarity: Double): Boolean {
        if (rarity <= 0.0) return false
        if (rarity >= 1.0) return true
        val denom = floor(1.0 / rarity).toInt().coerceAtLeast(2)
        return OsrsRandom.nextInt(denom) == 0
    }
}
