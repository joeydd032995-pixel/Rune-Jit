package skills.mining

import entity.Skill
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers Mining object interactions for all rocks in mining.yaml.
 * All XP values, level requirements, and tick rates are loaded from the data file —
 * none are hardcoded here. Source: server-skills.md rule.
 */
class MiningPlugin(
    private val yamlPath: Path = Path.of("data/skills/mining.yaml"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        val defs = MiningDefs.init(yamlPath)

        for ((_, rock) in defs.rocks) {
            if (rock.objectIds.isEmpty()) continue  // object IDs pending cache extraction
            ctx.onObjectInteract("Mine", rock.objectIds) { player, _ ->
                val miningLevel = player.skills.getLevel(Skill.MINING)

                if (miningLevel < rock.levelRequired) {
                    // TODO: send "You need level X Mining to mine this rock." message via packet
                    return@onObjectInteract
                }

                val action = MiningAction(player, rock, player.tickQueue, defs)
                player.tickQueue.schedule(defs.meta.ticksPerAttempt, action)
            }
        }
    }
}
