package skills.smithing

import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One Smithing cycle. Two modes:
 *   - [smelt] a [BarDef] at a furnace: consume ores, produce a bar, award smelt XP.
 *   - [smith] a [SmithItemDef] at an anvil: consume bars, produce the item, award smith XP.
 *
 * Rescheduled every [SmithingMeta.ticksPerAttempt] ticks until inputs are exhausted,
 * inventory is full, or the player stops. All values are loaded from smithing.yaml.
 *
 * Smithing has no failure roll — every attempt that passes the input checks succeeds.
 * Source: https://oldschool.runescape.wiki/w/Smithing
 */
class SmithingAction private constructor(
    private val player: Player,
    private val tickQueue: TickQueue,
    private val defs: SmithingConfig,
    private val bar: BarDef?,
    private val item: SmithItemDef?,
) : TickEvent {

    private var active = true

    companion object {
        /** Smelt a bar at a furnace. */
        fun smelt(player: Player, bar: BarDef, tickQueue: TickQueue, defs: SmithingConfig) =
            SmithingAction(player, tickQueue, defs, bar = bar, item = null)

        /** Smith an item at an anvil. */
        fun smith(player: Player, item: SmithItemDef, tickQueue: TickQueue, defs: SmithingConfig) =
            SmithingAction(player, tickQueue, defs, bar = null, item = item)
    }

    override fun process(currentTick: Long): Boolean {
        if (!active) return false

        val produced = if (bar != null) smelt(bar) else if (item != null) smith(item) else false
        if (!produced) {
            active = false
            return false
        }

        tickQueue.schedule(defs.meta.ticksPerAttempt, this)
        return true
    }

    /**
     * Smelts a single bar: validate level + ore counts, consume ores, produce the bar, award XP.
     * Returns false if the attempt could not be performed (stops the loop).
     * Source: https://oldschool.runescape.wiki/w/Smithing#Smelting
     */
    private fun smelt(bar: BarDef): Boolean {
        if (player.skills.getLevel(Skill.SMITHING) < bar.levelRequired) return false

        // Verify all ore ingredients are present in the required quantity before consuming any.
        for (ing in bar.ingredients) {
            if (!hasQuantity(ing.oreItemId, ing.qty)) return false
        }
        if (player.inventory.isFull()) return false

        for (ing in bar.ingredients) {
            if (!player.inventory.remove(ing.oreItemId, ing.qty)) return false
        }
        player.inventory.addItem(bar.barItemId)
        player.skills.addXp(Skill.SMITHING, bar.smeltXp)
        return true
    }

    /**
     * Smiths a single item: validate level + hammer + bar count, consume bars, produce the item, award XP.
     * Returns false if the attempt could not be performed (stops the loop).
     * Source: https://oldschool.runescape.wiki/w/Smithing#Smithing_2
     */
    private fun smith(item: SmithItemDef): Boolean {
        if (player.skills.getLevel(Skill.SMITHING) < item.levelRequired) return false
        if (!player.inventory.contains(defs.meta.hammerItemId)) return false

        val barDef = defs.bars[item.barType] ?: return false
        if (!hasQuantity(barDef.barItemId, item.barsRequired)) return false
        if (player.inventory.isFull()) return false

        if (!player.inventory.remove(barDef.barItemId, item.barsRequired)) return false
        player.inventory.addItem(item.outputItemId, item.outputQty)
        player.skills.addXp(Skill.SMITHING, item.smithXp)
        return true
    }

    /** True when the inventory holds at least [qty] of [itemId] in a single stackable slot. */
    private fun hasQuantity(itemId: Int, qty: Int): Boolean {
        var slot = 0
        var total = 0
        while (slot < 28) {
            val s = player.inventory.getSlot(slot)
            if (s != null && s.itemId == itemId) total += s.quantity
            slot++
        }
        return total >= qty
    }

    fun cancel() {
        active = false
    }
}
