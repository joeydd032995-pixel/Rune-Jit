package skills.mining

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One mining cycle: finds best pickaxe, rolls success, awards XP/ore, rolls gem and pet drops.
 * Rescheduled every [ticksPerAttempt] ticks until the player stops or inventory fills.
 *
 * Success formula: rand(255) < floor((miningLevel * 2 + pickaxe.miningBonus) / rock.difficulty)
 * Source: https://oldschool.runescape.wiki/w/Mining#Success_chance
 */
class MiningAction(
    private val player: Player,
    private val rock: RockDef,
    private val tickQueue: TickQueue,
    private val defs: MiningConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        // Stop if inventory is full
        if (player.inventory.isFull()) {
            active = false
            return false
        }

        val level = player.skills.getBoostedLevel(Skill.MINING)

        // Stop if level too low (shouldn't normally happen mid-session, but guard anyway)
        if (level < rock.levelRequired) {
            active = false
            return false
        }

        // Find best pickaxe available (equipped weapon slot 3 first, then inventory)
        val pickaxe = selectBestPickaxe(player, defs.pickaxes, level) ?: run {
            // No usable pickaxe — stop action
            // TODO: send "You need a pickaxe to mine this rock." chat message via packet
            active = false
            return false
        }

        // Success roll — Source: https://oldschool.runescape.wiki/w/Mining#Success_chance
        val threshold = (level * 2 + pickaxe.miningBonus) / rock.difficulty
        val success = OsrsRandom.nextInt(255) < threshold

        if (success) {
            player.skills.addXp(Skill.MINING, rock.xp)
            player.inventory.addItem(rock.oreItemId)
            rollGemDrop()
            rollRockGolemPet()
        }

        // Reschedule for next attempt regardless of success
        tickQueue.schedule(defs.meta.ticksPerAttempt, this)
        return false
    }

    /**
     * Selects the best pickaxe available to the player.
     * Checks equipped weapon slot first (slot 3), then scans inventory.
     * Returns the highest-miningBonus pickaxe the player has the Mining level to wield.
     * Source: https://oldschool.runescape.wiki/w/Pickaxe
     */
    private fun selectBestPickaxe(player: Player, pickaxes: List<PickaxeDef>, miningLevel: Int): PickaxeDef? {
        val available = pickaxes.filter { pick ->
            val equippedInWeaponSlot = player.equipment[3] == pick.itemId
            val inInventory = player.inventory.contains(pick.itemId)
            (equippedInWeaponSlot || inInventory) && miningLevel >= pick.wieldLevel
        }
        return available.maxByOrNull { it.miningBonus }
    }

    /**
     * Rolls the gem drop table on a 1/[GemDropConfig.baseDropChance] chance.
     * Gem is selected by weighted random from the gem table.
     * Source: https://oldschool.runescape.wiki/w/Uncut_gem#Gem_rocks
     */
    private fun rollGemDrop() {
        if (OsrsRandom.nextInt(defs.gemDrops.baseDropChance) != 0) return
        if (player.inventory.isFull()) return

        val totalWeight = defs.gemDrops.gems.sumOf { it.weight }
        var roll = OsrsRandom.nextInt(totalWeight)
        for (gem in defs.gemDrops.gems) {
            roll -= gem.weight
            if (roll < 0) {
                player.inventory.addItem(gem.itemId)
                return
            }
        }
        // Fallback: give first gem (should not be reached with a well-formed weight table)
        defs.gemDrops.gems.firstOrNull()?.let { player.inventory.addItem(it.itemId) }
    }

    /**
     * Rolls for the Rock Golem pet.
     * Rate scales with Mining level — higher level yields a better (lower) denominator.
     * Source: https://oldschool.runescape.wiki/w/Rock_golem#Drop_rate
     */
    private fun rollRockGolemPet() {
        if (player.inventory.isFull()) return
        val miningLevel = player.skills.getLevel(Skill.MINING)
        val scaledRate = if (defs.pet.scalesWithLevel) {
            // Higher level → lower effective denominator (better chance)
            (defs.pet.baseRate / (99 - miningLevel + 1).coerceAtLeast(1))
        } else {
            defs.pet.baseRate
        }
        if (OsrsRandom.nextInt(scaledRate) == 0) {
            player.inventory.addItem(defs.pet.itemId)
        }
    }

    fun stop() {
        active = false
    }
}
