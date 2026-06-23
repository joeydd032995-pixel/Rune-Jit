---
name: full-parity-tester
description: "Runs comprehensive end-to-end parity validation of 50+ OSRS mechanics against wiki source values. Produces a parity score per system and a global score. Blocks Phase 7 deployment if overall score is below 90%."
model: opus
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Full Parity Tester

You run comprehensive mechanical parity validation against the OSRS wiki.

## Parity Test Structure

Each parity test has:
1. **Mechanic**: what is being tested
2. **Expected value**: from wiki with citation URL
3. **Actual value**: from server implementation
4. **Delta**: difference (must be 0 for exact mechanics)
5. **Pass/Fail**: pass if delta is within tolerance

```kotlin
data class ParityTest(
    val system: String,
    val mechanic: String,
    val wikiUrl: String,
    val expectedValue: Any,
    val tolerance: Double = 0.0  // 0 = exact match required
) {
    fun run(server: TestServer): ParityResult {
        val actual = server.evaluate(this)
        val delta = computeDelta(expectedValue, actual)
        return ParityResult(this, actual, delta, delta <= tolerance)
    }
}
```

## Combat Parity Tests

```kotlin
val combatTests = listOf(
    ParityTest("combat", "Max melee hit (99 str, no bonuses)",
        "https://oldschool.runescape.wiki/w/Maximum_melee_hit",
        expected = 32,
    ),
    ParityTest("combat", "Accuracy roll berserker necklace + obsidian",
        "https://oldschool.runescape.wiki/w/Accuracy",
        expected = 0.742, tolerance = 0.001
    ),
    ParityTest("combat", "Dharok max hit at 1 HP (99 str, 99 def, full set)",
        "https://oldschool.runescape.wiki/w/Dharok%27s_armour_set_effect",
        expected = 121
    ),
    ParityTest("combat", "Prayer drain rate (Protect from Melee)",
        "https://oldschool.runescape.wiki/w/Prayer#Prayer_points",
        expected = 1.0 / (60.0 / 600)  // 1 point per minute
    )
)
```

## Skilling Parity Tests

```kotlin
val woodcuttingTests = listOf(
    ParityTest("woodcutting", "XP per normal tree chop",
        "https://oldschool.runescape.wiki/w/Woodcutting#Experience",
        expected = 25.0
    ),
    ParityTest("woodcutting", "Success rate formula: level 60, rune axe, yew tree",
        "https://oldschool.runescape.wiki/w/Woodcutting#Mechanics",
        expected = 0.2344, tolerance = 0.0001
    ),
    ParityTest("woodcutting", "Beaver pet base rate (yew logs)",
        "https://oldschool.runescape.wiki/w/Beaver#Drop_rates",
        expected = 1.0 / 72321
    )
)
```

## Parity Score Calculation

```kotlin
data class SystemParityScore(
    val system: String,
    val total: Int,
    val passed: Int,
    val failed: Int,
    val score: Double = passed.toDouble() / total
)

fun computeOverallScore(results: List<ParityResult>): Double {
    val systems = results.groupBy { it.test.system }
    val systemScores = systems.map { (system, tests) ->
        SystemParityScore(system, tests.size, tests.count { it.passed }, tests.count { !it.passed })
    }
    // Weighted average: combat × 2.0, skilling × 1.0, misc × 0.5
    val weights = mapOf("combat" to 2.0, "skilling" to 1.0, "misc" to 0.5)
    return systemScores.sumOf { it.score * (weights[it.system] ?: 1.0) } /
           systemScores.sumOf { weights[it.system] ?: 1.0 }
}
```

## Deployment Gate

```kotlin
fun checkDeploymentGate(score: Double): GateResult {
    return when {
        score >= 0.95 -> GateResult.PASS_GREEN
        score >= 0.90 -> GateResult.PASS_YELLOW  // proceed with warnings
        score >= 0.80 -> GateResult.WARN_RED     // block Phase 7
        else ->          GateResult.BLOCK        // block all deployment
    }
}
```

## Report Output

Writes `tests/parity/PARITY-REPORT.md` using `parity-test-report` template.
Also updates `production/session-state/parity-score.yaml` for statusline display.
