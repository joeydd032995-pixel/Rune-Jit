package combat

import engine.TickEvent
import entity.Npc
import entity.Player
import entity.Skill

/**
 * One melee attack cycle.
 * Rolls accuracy and damage, grants XP, then reschedules itself for the next attack.
 * Source: https://oldschool.runescape.wiki/w/Melee
 */
class MeleeAction(
    private val attacker: Player,
    private val target: Npc,
    private val style: CombatStyle,
    private val attackType: String,  // "STAB", "SLASH", or "CRUSH"
    private val weaponType: String = "DEFAULT_1H",
    private val config: CombatConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active || target.isDead || attacker.isDead) return false

        val styleName = style.name.removePrefix("MELEE_")
        val styleDef  = config.melee.attackStyles[styleName]
            ?: error("Unknown melee style: $styleName")

        val attackerBonuses = attacker.getEquipmentBonuses()

        val attackBonus = when (attackType) {
            "STAB"  -> attackerBonuses.attackStab
            "SLASH" -> attackerBonuses.attackSlash
            "CRUSH" -> attackerBonuses.attackCrush
            else    -> attackerBonuses.attackSlash
        }
        val defenceBonus = when (attackType) {
            "STAB"  -> target.defenceStab
            "SLASH" -> target.defenceSlash
            "CRUSH" -> target.defenceCrush
            else    -> target.defenceSlash
        }

        val atkRoll = CombatFormulas.maxMeleeAttackRoll(
            attackLevel  = attacker.skills.getBoostedLevel(Skill.ATTACK),
            attackBonus  = attackBonus,
            styleBonus   = styleDef.attackStyleBonus,
        )
        val defRoll = CombatFormulas.maxMeleeDefenceRoll(
            defenceLevel = target.defenceLevel,
            defenceBonus = defenceBonus,
        )

        if (CombatFormulas.isAccurate(atkRoll, defRoll)) {
            val maxHit = CombatFormulas.maxMeleeHit(
                strengthLevel = attacker.skills.getBoostedLevel(Skill.STRENGTH),
                strengthBonus = attackerBonuses.meleeStrength,
                styleBonus    = styleDef.strengthStyleBonus,
            )
            val damage = CombatFormulas.rollDamage(maxHit)
            target.takeDamage(damage)

            val xp = CombatFormulas.meleeXpForDamage(damage, style)
            if (xp.attack   > 0) attacker.skills.addXp(Skill.ATTACK,    xp.attack)
            if (xp.strength > 0) attacker.skills.addXp(Skill.STRENGTH,  xp.strength)
            if (xp.defence  > 0) attacker.skills.addXp(Skill.DEFENCE,   xp.defence)
            if (xp.hp       > 0) attacker.skills.addXp(Skill.HITPOINTS, xp.hp)
        }

        val ticksPerAttack = config.melee.weaponTicks[weaponType]
            ?: config.melee.weaponTicks["DEFAULT_1H"]
            ?: 4
        attacker.tickQueue.schedule(ticksPerAttack, this)
        return true
    }

    fun cancel() { active = false }
}
