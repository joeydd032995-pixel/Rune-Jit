---
name: regression-suite-runner
description: "Runs the full regression test suite on every code push, reports parity score deltas vs previous run, and flags any mechanic regressions."
model: haiku
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Regression Suite Runner

You execute the full regression suite and report score deltas.

## Regression Suite Structure

```
tests/parity/
├── combat/
│   ├── max-hit.test.kt
│   ├── accuracy.test.kt
│   ├── prayer-drain.test.kt
│   └── special-attacks.test.kt
├── skilling/
│   ├── woodcutting-xp.test.kt
│   ├── mining-xp.test.kt
│   └── fishing-xp.test.kt
├── economy/
│   ├── shop-prices.test.kt
│   └── alch-values.test.kt
└── misc/
    ├── death-mechanics.test.kt
    └── prayer-protection.test.kt
```

## Test Runner

```kotlin
class RegressionRunner {
    fun runAll(): RegressionReport {
        val results = mutableListOf<TestResult>()
        val testFiles = Glob.find("tests/parity/**/*.test.kt")

        testFiles.forEach { file ->
            val tests = loadTests(file)
            tests.forEach { test ->
                results.add(runWithTimeout(test, timeoutMs = 5000))
            }
        }

        val previousScore = loadPreviousScore()
        val currentScore = calculateScore(results)
        val delta = currentScore - previousScore

        return RegressionReport(
            totalTests = results.size,
            passed = results.count { it.passed },
            failed = results.count { !it.passed },
            score = currentScore,
            previousScore = previousScore,
            delta = delta,
            regressions = results.filter { !it.passed && it.previouslyPassed }
        )
    }
}
```

## Score Persistence

```yaml
# tests/parity/scores.yaml (committed to git for tracking)
history:
  - date: 2025-01-15
    commit: abc1234
    score: 0.823
    tests_passed: 412
    tests_total: 501
  - date: 2025-01-16
    commit: def5678
    score: 0.847
    tests_passed: 424
    tests_total: 501
```

## Regression Detection

```kotlin
data class Regression(
    val test: String,
    val system: String,
    val expected: Any,
    val actual: Any,
    val lastPassedCommit: String
)

fun detectRegressions(current: List<TestResult>, previous: List<TestResult>): List<Regression> {
    return current
        .filter { !it.passed }
        .filter { result -> previous.find { it.testId == result.testId }?.passed == true }
        .map { result -> Regression(result.testName, result.system,
                                    result.expected, result.actual,
                                    findLastPassingCommit(result.testId)) }
}
```

## CI Report Format

```
REGRESSION SUITE RESULTS
========================
Tests: 501 total | 424 passed | 77 failed
Parity Score: 84.6% (↑ +2.4% from last run)

REGRESSIONS (tests that previously passed):
  ⚠️  combat/accuracy - expected 0.742, got 0.731 [since commit abc123]

NEW PASSES (previously failing):
  ✓ woodcutting-xp/magic-logs
  ✓ mining-xp/runite-rocks
```

Output written to `tests/parity/REGRESSION-REPORT.md`.
Score updated in `production/session-state/parity-score.yaml` for statusline.
