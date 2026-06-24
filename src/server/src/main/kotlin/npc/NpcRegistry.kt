package npc

import entity.Npc
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Global registry for all active NPC instances.
 *
 * Each NPC is assigned a unique integer index on registration. The registry
 * is thread-safe via [ConcurrentHashMap] and [AtomicInteger], supporting
 * concurrent access from tick processing and network handler threads.
 *
 * Source: https://oldschool.runescape.wiki/w/Non-player_character
 */
object NpcRegistry {
    private val npcs = ConcurrentHashMap<Int, Npc>()
    private val nextIndex = AtomicInteger(1)

    /**
     * Registers [npc] and returns its assigned index.
     * The index is used for NPC update blocks in the game protocol.
     */
    fun register(npc: Npc): Int {
        val index = nextIndex.getAndIncrement()
        npcs[index] = npc
        return index
    }

    /** Returns the [Npc] with the given [index], or null if not registered. */
    fun get(index: Int): Npc? = npcs[index]

    /** Returns a snapshot of all currently registered NPCs. */
    fun all(): Collection<Npc> = npcs.values

    /** Removes the NPC at [index] from the registry (e.g. after death + despawn). */
    fun remove(index: Int) { npcs.remove(index) }

    /** Clears all NPCs and resets the index counter. For testing only. */
    fun clear() { npcs.clear(); nextIndex.set(1) }
}
