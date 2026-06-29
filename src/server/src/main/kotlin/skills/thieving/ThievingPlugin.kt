package skills.thieving

import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

class ThievingPlugin(
    private val yamlPath: Path = Path.of("data/skills/thieving.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = ThievingDefs.init(yamlPath)

        for ((_, def) in defs.pickpockets) {
            if (def.npcIds.isEmpty()) continue  // NPC IDs pending cache extraction
            ctx.onNpcInteract("Pickpocket", def.npcIds) { player, _ ->
                if (player.skills.getBoostedLevel(Skill.THIEVING) < def.levelRequired) return@onNpcInteract
                val action = PickpocketAction(player, def, player.tickQueue, defs)
                player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
            }
        }

        for ((_, def) in defs.stalls) {
            if (def.objectId == 0) continue  // object ID pending cache extraction
            ctx.onObjectInteract("Steal from", intArrayOf(def.objectId)) { player, _ ->
                if (player.skills.getBoostedLevel(Skill.THIEVING) < def.levelRequired) return@onObjectInteract
                val action = StallAction(player, def, player.tickQueue, defs)
                player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
            }
        }
    }
}
