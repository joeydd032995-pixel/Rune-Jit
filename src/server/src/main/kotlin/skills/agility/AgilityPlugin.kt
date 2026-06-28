package skills.agility

import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

class AgilityPlugin(
    private val yamlPath: Path = Path.of("data/skills/agility.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = AgilityDefs.init(yamlPath)

        for ((_, course) in defs.courses) {
            for ((index, obstacle) in course.obstacles.withIndex()) {
                if (obstacle.objectId == 0) continue  // object ID pending cache extraction
                val isLast = index == course.obstacles.size - 1
                ctx.onObjectInteract("Use", intArrayOf(obstacle.objectId)) { player, _ ->
                    if (player.skills.getBoostedLevel(Skill.AGILITY) < course.levelRequired) {
                        return@onObjectInteract
                    }
                    val action = AgilityAction(player, course, obstacle, isLast, player.tickQueue, defs)
                    player.tickQueue.schedule(obstacle.ticks, action)
                }
            }
        }
    }
}
