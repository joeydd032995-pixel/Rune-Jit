package skills.woodcutting

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill
import skills.firemaking.FiremakingDefs

/**
 * One woodcutting cycle: rolls success, awards XP/log, checks depletion.
 * Rescheduled every [ticksPerAttempt] ticks until the tree depletes or the player stops.
 *
 * Success formula: rand(255) < floor((wcLevel * 2 + axe.wcBonus) / tree.difficulty)
 * Source: https://oldschool.runescape.wiki/w/Woodcutting#Success_chance
 */
class WoodcuttingAction(
    private val player: Player,
    private val tree: TreeDef,
    private val axe: AxeDef,
    private val tickQueue: TickQueue,
    private val defs: WoodcuttingConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        val boostedLevel = player.skills.getBoostedLevel(Skill.WOODCUTTING)

        // Success roll — Source: https://oldschool.runescape.wiki/w/Woodcutting#Success_chance
        val threshold = (boostedLevel * 2 + axe.wcBonus) / tree.difficulty
        val success = OsrsRandom.nextInt(255) < threshold

        if (success) {
            awardXp()
            if (tree.dropsLog) {
                deliverLog()
            }
            rollBirdNest()
            rollBeaverPet()
            if (checkDepletion()) {
                active = false
                return false
            }
        }

        // Reschedule for next attempt
        tickQueue.schedule(defs.meta.ticksPerAttempt, this)
        return true
    }

    // XP calculation with optional boosts.
    // Guild: +7% Source: https://oldschool.runescape.wiki/w/Woodcutting_Guild
    // Lumberjack: +2.5% max Source: https://oldschool.runescape.wiki/w/Lumberjack_outfit
    private fun awardXp() {
        var xp = tree.xp
        if (player.isInWoodcuttingGuild) xp *= 1.07
        xp *= lumberjackMultiplier()
        player.skills.addXp(Skill.WOODCUTTING, xp)
    }

    private fun lumberjackMultiplier(): Double {
        // TODO: check equipped lumberjack pieces once equipment system is full
        // Placeholder: no bonus until equipment piece lookup is wired
        return 1.0
    }

    private fun deliverLog() {
        val logId = tree.logItemId ?: return

        // Infernal axe: ~1/3 chance to burn the log instead of banking it.
        // Source: https://oldschool.runescape.wiki/w/Infernal_axe
        if (axe.burnsLogs && OsrsRandom.nextInt(3) == 0) {
            if (FiremakingDefs.isInitialized) {
                val fmXp = (FiremakingDefs.config.byItemId[logId]?.xp ?: 0.0) * axe.firemakingXpMultiplier
                if (fmXp > 0.0) player.skills.addXp(Skill.FIREMAKING, fmXp)
            }
            return
        }

        if (player.inventory.isFull()) {
            // Inventory full — stop chopping
            active = false
            return
        }
        player.inventory.addItem(logId)
    }

    private fun rollBirdNest() {
        if (!tree.dropsLog) return
        // Source: https://oldschool.runescape.wiki/w/Bird_nest#Obtaining — 1/256 per log
        if (OsrsRandom.nextInt(defs.birdNests.baseDropChance) != 0) return
        val nestId = when (OsrsRandom.nextInt(3)) {
            0 -> defs.birdNests.itemIdSeeds
            1 -> defs.birdNests.itemIdRing
            else -> defs.birdNests.itemIdEmpty
        }
        // Bird nests always drop as physical ground items (never noted).
        // TODO: spawn as ground item at player tile once world ground-item system exists.
        // For now add to inventory as a placeholder if space available.
        player.inventory.addItem(nestId)
    }

    private fun rollBeaverPet() {
        // Source: https://oldschool.runescape.wiki/w/Beaver — ~1/72,000 at level 99 (unconfirmed)
        val wcLevel = player.skills.getLevel(Skill.WOODCUTTING)
        val scaledRate = if (defs.pet.scalesWithLevel) {
            // Higher level → lower effective rate denominator (better chance)
            (defs.pet.baseRate * (99.0 / wcLevel.coerceAtLeast(1))).toInt()
        } else {
            defs.pet.baseRate
        }
        if (OsrsRandom.nextInt(scaledRate) == 0) {
            player.inventory.addItem(defs.pet.itemId)
        }
    }

    private fun checkDepletion(): Boolean {
        // Blisterwood never depletes. Source: https://oldschool.runescape.wiki/w/Blisterwood_tree
        if (tree.respawnTicks == null) return false

        // Simplified depletion roll. Full per-tree depletion probability from cache pending.
        // Using a uniform 1/8 placeholder until object data is extracted.
        return OsrsRandom.nextInt(8) == 0
    }

    fun cancel() {
        active = false
    }
}
