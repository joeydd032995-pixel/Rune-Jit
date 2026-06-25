package combat

import npc.NpcRegistry
import org.slf4j.LoggerFactory
import plugins.Plugin
import plugins.PluginContext
import java.nio.file.Path

/**
 * Registers combat interactions with the plugin system.
 *
 * Dispatches the player's "Attack" NPC interaction to the appropriate combat
 * action based on their current combat state:
 *   - [selectedSpell] set → [MagicAction]
 *   - [CombatStyle] starts with "RANGED_" → [RangedAction]
 *   - Otherwise → [MeleeAction]
 *
 * Weapon type and default attack type are resolved from [WeaponDefs] using the
 * item in the player's weapon slot (equipment index 3).
 *
 * Source: https://oldschool.runescape.wiki/w/Combat
 */
class CombatPlugin(
    private val dataDir: Path = Path.of("data/combat"),
) : Plugin() {

    override fun register(ctx: PluginContext) {
        CombatDefs.init(dataDir)
        WeaponDefs.init(dataDir)

        ctx.onNpcInteract("Attack", IntArray(0)) { player, npcIndex ->
            val npc = NpcRegistry.get(npcIndex) ?: run {
                log.debug("Attack ignored: NPC index {} not found in registry", npcIndex)
                return@onNpcInteract
            }
            if (npc.isDead) return@onNpcInteract

            // Cancel any existing combat action before starting a new one.
            player.activeCombatAction?.cancel()

            // Resolve weapon properties from the item in the weapon slot (index 3).
            // Source: https://oldschool.runescape.wiki/w/Equipment#Slot
            val weaponDef = WeaponDefs.getByItemId(player.equipment[3])
            player.weaponType = weaponDef.weaponType
            player.attackType = weaponDef.defaultAttackType

            val spell = player.selectedSpell
            val config = CombatDefs.config

            val action: CombatAction = when {
                spell != null -> {
                    if (config.standardBook.spells[spell] == null) {
                        log.warn("Player {} has unknown autocast spell '{}' — combat cancelled", player.username, spell)
                        return@onNpcInteract
                    }
                    MagicAction(player, npc, spell, config)
                }
                player.combatStyle.name.startsWith("RANGED_") -> {
                    RangedAction(player, npc, player.combatStyle, player.weaponType, config)
                }
                else -> {
                    MeleeAction(player, npc, player.combatStyle, player.attackType, player.weaponType, config)
                }
            }

            player.activeCombatAction = action
            // Schedule for next tick — first attack fires 1 tick after the click.
            // Source: https://oldschool.runescape.wiki/w/Combat#Attack_speed
            player.tickQueue.schedule(1, action)

            log.debug(
                "{} attacks NPC {} ({}) — style={} weapon={}",
                player.username, npc.name, npcIndex,
                player.combatStyle, player.weaponType,
            )
        }

        log.info("CombatPlugin loaded — NPC attack handler registered (wildcard)")
    }

    companion object {
        private val log = LoggerFactory.getLogger(CombatPlugin::class.java)
    }
}
