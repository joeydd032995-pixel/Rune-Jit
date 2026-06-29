package skills.runecraft

import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

/**
 * One Runecraft action: consumes all essence in the inventory, produces runes, and awards XP.
 * All essence is consumed in a single tick — there is no per-essence tick cycle.
 * The multiple-rune threshold determines how many runes each essence yields based on level.
 * Source: https://oldschool.runescape.wiki/w/Runecraft
 */
class RunecraftAction(
    private val player: Player,
    private val altar: AltarDef,
    private val defs: RunecraftConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false
        active = false

        val rcLevel = player.skills.getBoostedLevel(Skill.RUNECRAFT)

        if (rcLevel < altar.levelRequired) {
            return false
        }

        // Count all essence of the required type in the inventory
        var essenceCount = 0
        for (slot in 0 until 28) {
            val slotData = player.inventory.getSlot(slot)
            if (slotData?.itemId == altar.essenceItemId) {
                essenceCount += slotData.quantity
            }
        }

        if (essenceCount == 0) {
            return false
        }

        // Consume all essence
        player.inventory.remove(altar.essenceItemId, essenceCount)

        // Determine runes per essence from the multiple-rune threshold table
        // Source: https://oldschool.runescape.wiki/w/Runecraft#Multiple_runes_per_essence
        val runesPerEssence = altar.runesPerEssence(rcLevel)
        val totalRunes = runesPerEssence * essenceCount
        player.inventory.addItem(altar.runeItemId, totalRunes)

        // Award XP — flat xpPerEssence per essence consumed, NOT per rune produced
        // Source: https://oldschool.runescape.wiki/w/Runecraft#Experience
        player.skills.addXp(Skill.RUNECRAFT, altar.xpPerEssence * essenceCount)

        return false // one-shot: all essence consumed in a single action
    }

    fun cancel() {
        active = false
    }
}
