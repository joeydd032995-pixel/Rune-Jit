package combat

import engine.OsrsRandom
import entity.Npc
import entity.Player
import entity.Skill

/**
 * One magic combat cycle.
 * Rolls accuracy, rolls damage on hit (0 on miss), grants magic + HP XP.
 * Always grants base spell XP even on a miss.
 * Source: https://oldschool.runescape.wiki/w/Magic#Combat
 */
class MagicAction(
    private val attacker: Player,
    private val target: Npc,
    private val spellKey: String,
    private val config: CombatConfig,
) : CombatAction {

    private var active = true

    override fun process(currentTick: Long): Boolean {
        if (!active || target.isDead || attacker.isDead) return false

        val spell = config.standardBook.spells[spellKey]
            ?: error("Unknown spell: $spellKey")

        val attackerBonuses = attacker.getEquipmentBonuses()

        // Prayer multipliers wired from player's active prayer set.
        // Source: https://oldschool.runescape.wiki/w/Prayer#Bonuses
        val atkRoll = CombatFormulas.maxMagicAttackRoll(
            magicLevel       = attacker.skills.getBoostedLevel(Skill.MAGIC),
            magicAttackBonus = attackerBonuses.attackMagic,
            prayerMult       = attacker.prayer.magicMult,
        )
        val defRoll = CombatFormulas.maxMeleeDefenceRoll(
            defenceLevel = target.defenceLevel,
            defenceBonus = target.defenceMagic,
        )

        val damage = if (CombatFormulas.isAccurate(atkRoll, defRoll)) {
            OsrsRandom.nextInt(spell.maxHit + 1)
        } else {
            0
        }

        if (damage > 0) target.takeDamage(damage)

        // Base XP granted even on miss; bonus 2 XP per damage point on hit.
        val xp = CombatFormulas.magicXpForDamage(damage, spell.baseXp)
        attacker.skills.addXp(Skill.MAGIC,     xp.magic)
        if (xp.hp > 0) attacker.skills.addXp(Skill.HITPOINTS, xp.hp)

        attacker.tickQueue.schedule(config.standardBook.combatCycleTicks, this)
        return true
    }

    override fun cancel() { active = false }
}
