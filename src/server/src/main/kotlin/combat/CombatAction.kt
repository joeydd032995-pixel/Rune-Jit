package combat

import engine.TickEvent

/**
 * Marker interface for all player-initiated combat actions.
 * Extends [TickEvent] so actions can be scheduled and auto-rescheduled
 * by the tick queue, and adds [cancel] so the active action can be
 * cleanly stopped when the player re-targets or dies.
 *
 * Source: https://oldschool.runescape.wiki/w/Combat
 */
interface CombatAction : TickEvent {
    /** Stops this action from rescheduling itself on the next tick. */
    fun cancel()
}
