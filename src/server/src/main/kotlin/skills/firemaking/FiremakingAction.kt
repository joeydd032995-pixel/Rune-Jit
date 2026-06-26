package skills.firemaking

import engine.TickEvent
import entity.Player
import entity.Skill

/**
 * One Firemaking cycle: consume a log, award XP, reschedule.
 *
 * Firemaking has a 100% success rate — every attempt lights the fire.
 * Consumes one log per cycle; stops when the log runs out or the tinderbox is gone.
 * Ash and fire placement are deferred (require world ground-item system).
 *
 * Source: https://oldschool.runescape.wiki/w/Firemaking
 */
class FiremakingAction(
    private val player: Player,
    private val logDef: LogDef,
    private val config: FiremakingConfig,
) : TickEvent {

    override fun process(currentTick: Long): Boolean {
        if (player.isDead) return false
        if (!player.inventory.contains(config.meta.tinderboxItemId)) return false
        if (!player.inventory.remove(logDef.itemId, 1)) return false  // no more logs of this type

        // 100% success — award XP immediately.
        // Source: https://oldschool.runescape.wiki/w/Firemaking#Logs
        player.skills.addXp(Skill.FIREMAKING, logDef.xp)

        player.tickQueue.schedule(config.meta.ticksPerAttempt, this)
        return true
    }
}
