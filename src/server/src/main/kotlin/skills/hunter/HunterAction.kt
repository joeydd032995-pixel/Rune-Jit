package skills.hunter

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One hunter trap check: rolls catch probability based on level, awards XP and loot on success.
 * Rescheduled every [creature.checkTicks] ticks until a catch occurs or the player cancels.
 *
 * Catch probability scales linearly from catch_rate_low at level_required to catch_rate_high at 99.
 * Source: https://oldschool.runescape.wiki/w/Hunter#Mechanics
 */
class HunterAction(
    private val player: Player,
    private val creature: CreatureDef,
    private val tickQueue: TickQueue,
    private val defs: HunterConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        val hunterLevel = player.skills.getBoostedLevel(Skill.HUNTER)
        if (hunterLevel < creature.levelRequired) {
            active = false
            return false
        }

        val resolution = defs.trapConfig.catchRateResolution
        val catchRate = creature.catchRateInt(hunterLevel, resolution)

        // Success roll — Source: https://oldschool.runescape.wiki/w/Hunter#Mechanics
        if (OsrsRandom.nextInt(resolution) < catchRate) {
            awardXp()
            deliverLoot()
            rollPet()
            active = false
            return false
        }

        // Not yet caught — reschedule
        tickQueue.schedule(creature.checkTicks, this)
        return true
    }

    private fun awardXp() {
        player.skills.addXp(Skill.HUNTER, creature.xp)
    }

    private fun deliverLoot() {
        if (creature.loot.isEmpty()) return
        val entry = rollLoot() ?: return
        if (!player.inventory.isFull()) {
            player.inventory.addItem(entry.itemId, entry.qty)
        }
    }

    private fun rollLoot(): HunterLootEntry? {
        val totalWeight = creature.loot.sumOf { it.weight }
        if (totalWeight <= 0) return creature.loot.firstOrNull()
        var roll = OsrsRandom.nextInt(totalWeight)
        for (entry in creature.loot) {
            roll -= entry.weight
            if (roll < 0) return entry
        }
        return creature.loot.last()
    }

    private fun rollPet() {
        // Source: https://oldschool.runescape.wiki/w/Baby_chinchompa
        val hunterLevel = player.skills.getLevel(Skill.HUNTER)
        val scaledRate = if (defs.pet.scalesWithLevel) {
            (defs.pet.baseRate * (99.0 / hunterLevel.coerceAtLeast(1))).toInt()
        } else {
            defs.pet.baseRate
        }
        if (scaledRate > 0 && OsrsRandom.nextInt(scaledRate) == 0) {
            player.inventory.addItem(defs.pet.itemId)
        }
    }

    fun cancel() {
        active = false
    }
}
