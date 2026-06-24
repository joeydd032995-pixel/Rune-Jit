package combat

/**
 * Data classes representing the parsed contents of the three combat YAML files.
 * Loaded once at startup by [CombatLoader].
 */

data class AttackStyleDef(
    val attackStyleBonus: Int,
    val strengthStyleBonus: Int,
    val defenceStyleBonus: Int,
    val xpSkill: String,
)

data class MeleeMeta(
    val combatXpPerDamage: Double,
    val hpXpPerDamage: Double,
    val attackStyles: Map<String, AttackStyleDef>,
    val weaponTicks: Map<String, Int>,
)

data class Spell(
    val name: String,
    val levelRequired: Int,
    val maxHit: Int,
    val baseXp: Double,
    val freezeTicks: Int? = null,
    val animationId: Int = -1,
)

data class SpellBook(
    val spells: Map<String, Spell>,
    val combatCycleTicks: Int,
    val hpXpPerDamage: Double,
    val magicXpPerDamage: Double,
)

data class RangedAttackStyleDef(
    val styleBonus: Int,
    val ticksReduction: Int = 0,
    val xpSkill: String,
)

data class RangedMeta(
    val rangedXpPerDamage: Double,
    val hpXpPerDamage: Double,
    val attackStyles: Map<String, RangedAttackStyleDef>,
    val weaponTicks: Map<String, Int>,
)

data class CombatConfig(
    val melee: MeleeMeta,
    val standardBook: SpellBook,
    val ranged: RangedMeta,
)
