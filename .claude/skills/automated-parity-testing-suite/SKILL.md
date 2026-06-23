---
name: automated-parity-testing-suite
description: "Runs the full parity test suite across all implemented systems, computes per-system scores, flags regressions from the previous run, and produces a parity-test-report.md with a deployment gate decision."
argument-hint: "[scope: all|combat|skills|networking|client|economy]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /automated-parity-testing-suite [scope]

Executes all parity tests, scores each system, detects regressions.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Server compiled (`./gradlew build`) | Yes |
| `tests/parity/` directory exists | Yes |
| Previous parity baseline (`tests/parity/baseline.yaml`) | Optional (first run if missing) |
| At least one system implemented | Yes |

Read `tests/parity/README.md` and `tests/parity/baseline.yaml` (if present) before proceeding.

## Phase 2: Scope Resolution

Resolve `[scope]` to a test list:

| Scope | Systems Tested |
|-------|---------------|
| `all` | combat, skills, networking, client, economy, instancing |
| `combat` | melee accuracy, ranged accuracy, magic accuracy, max hits, prayer bonuses, specials |
| `skills` | woodcutting, mining, fishing, agility, thieving, slayer, herblore, crafting, smithing, cooking |
| `networking` | login, packet opcodes, ISAAC, JS5, world-hop, reconnect |
| `client` | widget rendering, animation, minimap, camera, audio |
| `economy` | GE matching, shop pricing, alch values, NPC drop tables |

Spawn `regression-suite-runner` to load the previous baseline from `tests/parity/baseline.yaml`.

## Phase 3: Execute Parity Tests

Spawn `full-parity-tester` with scope to run:

```kotlin
data class ParityTest(
    val system: String,
    val mechanic: String,
    val wikiValue: Any,
    val wikiUrl: String,
    val actualValue: Any?,
    var status: TestStatus = TestStatus.PENDING
)

enum class TestStatus { PASS, FAIL, NOT_IMPLEMENTED, PARTIAL }

fun runSuite(scope: String): ParityReport {
    val tests = loadTests(scope)  // from tests/parity/**/*-parity-checklist.md
    tests.forEach { test ->
        test.status = evaluate(test)
    }
    return ParityReport(tests)
}
```

Test categories and targets:

| System | Pass Threshold | Notes |
|--------|---------------|-------|
| Combat | ≥95% | Critical — affects all content |
| Skills | ≥90% | Per-skill subtargets in GDD |
| Networking | ≥95% | Desync risk if below |
| Client | ≥85% | Visual parity, some subjectivity |
| Economy | ≥80% | GE prices fluctuate naturally |
| Instancing | ≥90% | Raid isolation required |

## Phase 4: Score Computation

Spawn `qa-parity-verifier` to compute weighted global score:

```kotlin
val systemWeights = mapOf(
    "combat"      to 0.25,
    "skills"      to 0.25,
    "networking"  to 0.20,
    "client"      to 0.10,
    "economy"     to 0.10,
    "instancing"  to 0.10
)

fun globalScore(results: Map<String, Double>): Double =
    systemWeights.entries.sumOf { (sys, weight) ->
        (results[sys] ?: 0.0) * weight
    }
```

## Phase 5: Regression Detection

Spawn `regression-suite-runner` to compare against baseline:

```yaml
# tests/parity/baseline.yaml format
combat:
  score: 0.92
  tests_passed: 184
  tests_total: 200
  recorded_at: "2024-01-01T00:00:00Z"
skills:
  score: 0.88
  # ...
```

Flag any system where current score < baseline score by more than 2%. Output:
```
⚠ REGRESSION: combat 92% → 89% (-3%) — 6 tests now failing
⚠ REGRESSION: networking 95% → 93% (-2%) — 4 tests now failing
✓ skills: 88% → 91% (+3%)
```

## Phase 6: Produce Report

Write `docs/architecture/parity-test-report.md` using `parity-test-report` template:

Sections:
1. **Executive Summary**: global score, pass/fail by system, regressions
2. **System Breakdown**: per-system table with score, delta, test counts
3. **Failing Tests**: mechanic name, expected, actual, wiki source URL
4. **Regressions**: what regressed and likely cause
5. **New Passes**: previously failing tests that now pass
6. **Deployment Gate**: PASS / FAIL recommendation

## Phase 7: Deployment Gate

```kotlin
fun deploymentGate(globalScore: Double, hasRegressions: Boolean): GateResult {
    return when {
        globalScore >= 0.90 && !hasRegressions -> GateResult.PASS
        globalScore >= 0.85 && !hasRegressions -> GateResult.CONDITIONAL_PASS
        hasRegressions -> GateResult.FAIL_REGRESSION
        else -> GateResult.FAIL_SCORE
    }
}
```

Update `tests/parity/baseline.yaml` only on PASS or CONDITIONAL_PASS.

## Error Recovery

| Error | Recovery |
|-------|---------|
| Test file missing | Skip system; mark as NOT_RUN in report |
| Server not running | Start server with `./gradlew run --daemon`; retry |
| Test timeout | Mark as NOT_IMPLEMENTED; log for investigation |
| Baseline missing | Create baseline from current run; mark first-run |

## Nuances

- Parity tests compare against wiki values, not against live OSRS — no legal risk
- `NOT_IMPLEMENTED` counts against the score (= 0% for that mechanic)
- Some mechanics are probabilistic (RNG) — use statistical sampling over 10,000 iterations
- XP values must be exact; even 0.1 XP difference = FAIL
- Combat prayer protection testing: overhead prayers reduce damage by 0 in PvM — test this explicitly

## Next Steps

1. Fix all regressions before next release
2. Run `/performance-profiling-and-optimization` if combat score passes
3. Update per-system GDDs with newly discovered discrepancies
4. Run `/raids-and-instanced-content-generator` parity tests after raid content added
