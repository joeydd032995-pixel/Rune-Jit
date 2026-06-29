package skills.agility

import engine.OsrsRandom
import engine.TickEvent
import engine.TickQueue
import entity.Player
import entity.Skill

class AgilityAction(
    private val player: Player,
    private val course: CourseDef,
    private val obstacle: ObstacleDef,
    private val isLastObstacle: Boolean,
    private val tickQueue: TickQueue,
    private val defs: AgilityConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active) return false
        active = false

        if (player.skills.getBoostedLevel(Skill.AGILITY) < course.levelRequired) return false

        player.skills.addXp(Skill.AGILITY, obstacle.xp)

        if (isLastObstacle) {
            player.skills.addXp(Skill.AGILITY, course.completionBonusXp)

            val markChance = course.markOfGraceChance
            if (markChance != null && markChance > 0) {
                if (OsrsRandom.nextInt(markChance) == 0) {
                    player.inventory.addItem(defs.meta.markOfGraceItemId, 1)
                }
            }
        }

        return false
    }

    fun cancel() {
        active = false
    }
}
