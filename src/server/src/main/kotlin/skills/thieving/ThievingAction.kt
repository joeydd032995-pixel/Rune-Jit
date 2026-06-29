package skills.thieving

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

class PickpocketAction(
    private val player: Player,
    private val def: PickpocketDef,
    private val tickQueue: TickQueue,
    private val defs: ThievingConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false
        active = false

        val level = player.skills.getBoostedLevel(Skill.THIEVING)
        if (level < def.levelRequired) return false

        val successRate = def.successRate(level)
        if (OsrsRandom.nextInt(256) < successRate) {
            player.skills.addXp(Skill.THIEVING, def.xp)
            val loot = rollLoot(def.loot)
            if (loot != null) player.inventory.addItem(loot.itemId, loot.qty)
        } else {
            // Failure: stun damage — stun tick-blocking wired by future input layer
            // Source: https://oldschool.runescape.wiki/w/Thieving#Pickpocketing
            player.takeDamage(def.stunDamage)
        }

        return false
    }

    fun cancel() { active = false }
}

class StallAction(
    private val player: Player,
    private val def: StallDef,
    private val tickQueue: TickQueue,
    private val defs: ThievingConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false
        active = false

        if (player.skills.getBoostedLevel(Skill.THIEVING) < def.levelRequired) return false

        // Stalls always succeed — Source: https://oldschool.runescape.wiki/w/Thieving#Stalls
        player.skills.addXp(Skill.THIEVING, def.xp)
        val loot = rollLoot(def.loot)
        if (loot != null) player.inventory.addItem(loot.itemId, loot.qty)

        // Stall depletion and respawn managed by the object/scenery system
        return false
    }

    fun cancel() { active = false }
}

private fun rollLoot(loot: List<LootEntry>): LootEntry? {
    if (loot.isEmpty()) return null
    val totalWeight = loot.sumOf { it.weight }
    var roll = OsrsRandom.nextInt(totalWeight)
    for (entry in loot) {
        roll -= entry.weight
        if (roll < 0) return entry
    }
    return loot.last()
}
