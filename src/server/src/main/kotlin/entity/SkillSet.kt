package entity

/**
 * Tracks base XP, derived levels, and temporary boosts for all 23 skills.
 * getBoostedLevel returns base level + any active boost (e.g. Dragon axe spec +3).
 */
class SkillSet {
    private val xp = DoubleArray(Skill.entries.size)
    private val boosts = IntArray(Skill.entries.size)

    fun getXp(skill: Skill): Double = xp[skill.ordinal]

    fun getLevel(skill: Skill): Int = xpToLevel(xp[skill.ordinal])

    fun getBoostedLevel(skill: Skill): Int =
        (getLevel(skill) + boosts[skill.ordinal]).coerceIn(1, 125)

    fun addXp(skill: Skill, amount: Double) {
        xp[skill.ordinal] = (xp[skill.ordinal] + amount).coerceAtMost(200_000_000.0)
    }

    /**
     * Applies a temporary level boost that expires after [durationTicks] ticks.
     * Callers are responsible for scheduling the expiry via TickQueue.
     */
    fun applyBoost(skill: Skill, amount: Int) {
        boosts[skill.ordinal] += amount
    }

    fun removeBoost(skill: Skill, amount: Int) {
        boosts[skill.ordinal] = (boosts[skill.ordinal] - amount).coerceAtLeast(0)
    }

    companion object {
        /** OSRS XP-to-level table. Source: https://oldschool.runescape.wiki/w/Experience */
        private val LEVEL_FOR_XP: IntArray = buildLevelTable()

        fun xpToLevel(xp: Double): Int {
            val intXp = xp.toInt()
            return LEVEL_FOR_XP.indexOfLast { it <= intXp }.coerceAtLeast(1)
        }

        private fun buildLevelTable(): IntArray {
            val table = IntArray(100)
            var points = 0.0
            for (level in 1..99) {
                points += Math.floor(level + 300.0 * Math.pow(2.0, level / 7.0))
                table[level] = (points / 4).toInt()
            }
            return table
        }
    }
}
