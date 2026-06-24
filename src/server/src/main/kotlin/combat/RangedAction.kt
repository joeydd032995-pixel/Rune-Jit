package combat

import engine.TickEvent
import entity.Npc
import entity.Player
import entity.Skill

/**
 * One ranged attack cycle.
 * Rolls accuracy and damage, grants XP, then reschedules itself.
 * RAPID style reduces the attack cycle by 1 tick.
 * Source: https://oldschool.runescape.wiki/w/Ranged
 */
class RangedAction(
    private val attacker: Player,
    private val target: Npc,
    private val style: CombatStyle,
    private val weaponType: String = "BOW_STANDARD",
    private val config: CombatConfig,
) : TickEvent {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active || target.isDead || attacker.isDead) return false

        val styleName = style.name.removePrefix("RANGED_")
        val styleDef  = config.ranged.attackStyles[styleName]
            ?: error("Unknown ranged style: $styleName")

        val attackerBonuses = attacker.getEquipmentBonuses()

        // Prayer multipliers wired from player's active prayer set.
        // Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
        val atkRoll = CombatFormulas.maxRangedAttackRoll(
            rangedLevel        = attacker.skills.getBoostedLevel(Skill.RANGED),
            rangedAttackBonus  = attackerBonuses.attackRanged,
            prayerMult         = attacker.prayer.rangedMult,
            styleBonus         = styleDef.styleBonus,
        )
        val defRoll = CombatFormulas.maxMeleeDefenceRoll(
            defenceLevel = target.defenceLevel,
            defenceBonus = target.defenceRanged,
        )

        if (CombatFormulas.isAccurate(atkRoll, defRoll)) {
            val maxHit = CombatFormulas.maxRangedHit(
                rangedLevel    = attacker.skills.getBoostedLevel(Skill.RANGED),
                rangedStrBonus = attackerBonuses.rangedStrength,
                prayerMult     = attacker.prayer.rangedStrengthMult,
                styleBonus     = styleDef.styleBonus,
            )
            val damage = CombatFormulas.rollDamage(maxHit)
            target.takeDamage(damage)

            val xp = CombatFormulas.rangedXpForDamage(damage)
            if (xp.ranged > 0) attacker.skills.addXp(Skill.RANGED,    xp.ranged)
            if (xp.hp     > 0) attacker.skills.addXp(Skill.HITPOINTS, xp.hp)
        }

        val baseTicks = config.ranged.weaponTicks[weaponType]
            ?: config.ranged.weaponTicks["BOW_STANDARD"]
            ?: 4
        val ticksPerAttack = (baseTicks - styleDef.ticksReduction).coerceAtLeast(1)
        attacker.tickQueue.schedule(ticksPerAttack, this)
        return true
    }

    fun cancel() { active = false }
}
