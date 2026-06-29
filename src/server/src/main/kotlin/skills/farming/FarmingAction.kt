package skills.farming

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * Tracks growth stages for a planted crop.
 * Reschedules every growthTicks ticks; awards plant XP on first stage transition.
 * Source: https://oldschool.runescape.wiki/w/Farming#Growth_stages
 */
class FarmingGrowthAction(
    private val player: Player,
    private val patch: PatchDef,
    private val tickQueue: TickQueue,
    private val defs: FarmingConfig,
) : TickEvent {

    private var currentStage = 0
    private var active = true
    private var plantXpAwarded = false

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        currentStage++

        if (!plantXpAwarded) {
            player.skills.addXp(Skill.FARMING, patch.plantXp)
            plantXpAwarded = true
        }

        if (currentStage >= patch.growthStages) {
            active = false
            return false
        }

        tickQueue.schedule(patch.growthTicks, this)
        return true
    }

    val isFullyGrown: Boolean get() = currentStage >= patch.growthStages

    fun harvest() {
        if (!isFullyGrown) return
        val yield = patch.baseYield + OsrsRandom.nextInt(3)
        for (i in 0 until yield) {
            if (player.inventory.isFull()) break
            player.inventory.addItem(patch.produceItemId, 1)
        }
        player.skills.addXp(Skill.FARMING, patch.harvestXp * yield)
        active = false
    }

    fun cancel() {
        active = false
    }
}
