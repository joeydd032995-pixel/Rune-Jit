package prayer

import engine.TickEvent
import entity.Player

/**
 * Recurring tick event that drains prayer points once per tick for [player].
 * Scheduled at login and reschedules itself every tick as long as the player
 * is alive (never self-cancels — the drain is a permanent fixture of player state).
 *
 * Prayer flicking: if the player activates and deactivates a prayer within the
 * same tick, [PrayerSet.tick] sees an empty active set for that tick and no drain
 * is applied, but combat bonuses for attacks processed that tick still benefit
 * from the brief activation. This is an intentional OSRS mechanic.
 * Source: https://oldschool.runescape.wiki/w/Prayer#Prayer_flicking
 *
 * No Thread.sleep — all scheduling via tick queue per server-tick.md.
 */
class PrayerDrainAction(private val player: Player) : TickEvent {

    override fun process(currentTick: Long): Boolean {
        val bonus = player.getEquipmentBonuses().prayerBonus
        player.prayer.tick(bonus)
        // Reschedule for next tick — prayer drain runs every tick.
        player.tickQueue.schedule(1, this)
        // TickQueueImpl ignores the boolean return; reschedule is explicit above.
        return false
    }
}
