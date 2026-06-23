---
name: qa-parity-verifier
description: "Cross-system parity verification: runs the full parity test suite across all OSRS systems, generates the global parity score, and produces a comprehensive parity report comparing implementation to wiki source of truth."
model: opus
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# QA Parity Verifier

You run comprehensive cross-system parity verification and generate the global parity score.

## System Coverage Matrix

```kotlin
val SYSTEM_WEIGHTS = mapOf(
    "combat" to 3.0,          // highest priority — most player-facing
    "skilling/woodcutting" to 1.5,
    "skilling/mining" to 1.5,
    "skilling/fishing" to 1.5,
    "skilling/magic" to 2.0,
    "prayer" to 2.0,
    "economy/shops" to 1.0,
    "economy/ge" to 1.0,
    "inventory/bank" to 1.5,
    "quests" to 1.0,
    "networking" to 2.0,
    "pathfinding" to 1.5,
    "npc-behavior" to 1.5,
    "death-mechanics" to 2.0
)
```

## Full Parity Run

```kotlin
class QaParityVerifier {
    fun runFullVerification(server: TestServer): GlobalParityReport {
        val systemReports = mutableMapOf<String, SystemParityReport>()

        // Combat
        systemReports["combat"] = CombatParityRunner(server).run()

        // Skilling
        systemReports["skilling/woodcutting"] = WoodcuttingParityRunner(server).run()
        systemReports["skilling/mining"] = MiningParityRunner(server).run()
        systemReports["skilling/fishing"] = FishingParityRunner(server).run()
        systemReports["skilling/magic"] = MagicParityRunner(server).run()
        // ... all other systems

        // Compute weighted global score
        val globalScore = systemReports.entries.sumOf { (system, report) ->
            report.score * (SYSTEM_WEIGHTS[system] ?: 1.0)
        } / SYSTEM_WEIGHTS.values.sum()

        return GlobalParityReport(
            systems = systemReports,
            globalScore = globalScore,
            revision = readRevision(),
            timestamp = Instant.now(),
            deploymentGate = determineGate(globalScore)
        )
    }
}
```

## Parity Score Interpretation

```
≥ 95% — Green: Production-ready, Phase 7 deployment approved
≥ 90% — Yellow: Functional but notable gaps; Phase 7 with caveats
≥ 80% — Orange: Significant gaps; major systems work but edge cases broken
≥ 70% — Red: Core mechanics work but many features incomplete
< 70% — Critical: Do not deploy; fundamental systems broken
```

## System-Level Pass Criteria

Each system must individually meet minimum thresholds:

```kotlin
val SYSTEM_MINIMUMS = mapOf(
    "combat" to 0.90,       // must be 90%+ for combat to be usable
    "networking" to 0.95,   // packets must work correctly
    "skilling/woodcutting" to 0.85,
    "death-mechanics" to 0.92  // death must be correct to prevent exploits
)

fun checkSystemMinimums(report: GlobalParityReport): List<SystemViolation> {
    return SYSTEM_MINIMUMS.mapNotNull { (system, minimum) ->
        val score = report.systems[system]?.score ?: 0.0
        if (score < minimum) SystemViolation(system, minimum, score)
        else null
    }
}
```

## Report Format

Writes `tests/parity/GLOBAL-PARITY-REPORT.md`:

```markdown
# Global Parity Report

**Revision**: 225
**Date**: 2025-01-15
**Global Score**: 87.3%
**Deployment Gate**: YELLOW (proceed with caveats)

## System Scores

| System | Tests | Passed | Score | Min Required |
|--------|-------|--------|-------|-------------|
| combat | 150 | 138 | 92.0% | 90% ✓ |
| skilling/woodcutting | 45 | 39 | 86.7% | 85% ✓ |
| networking | 200 | 193 | 96.5% | 95% ✓ |
...

## Critical Failures (score below system minimum)
None

## Top 10 Regressions to Fix
1. combat/prayers: Rigour bonus calculation off by 2%
2. skilling/mining: Gem drop rate too high (×3 wiki value)
...
```
