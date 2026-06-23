---
name: verify-mechanic-parity-1to1
description: "Runs parity tests for a specific OSRS system, comparing implementation against wiki source values. Produces a scored parity report and flags any regressions."
argument-hint: "[system: combat|woodcutting|mining|fishing|magic|prayer|economy|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: opus
---

# /verify-mechanic-parity-1to1 [system]

Validates a specific OSRS system against wiki source of truth.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Server compiled | `./gradlew build` |
| osrsbox data imported | `data/osrsbox/items-complete.json` |
| Parity test files exist | `tests/parity/[system]/` |

## Phase 2: Load Wiki Reference Values

Spawn `wiki-researcher` to:
- Fetch current wiki values for the target system
- Compare against data in `tests/parity/[system]/`
- Flag any values that have changed since last run

## Phase 3: Run Parity Tests

Spawn `full-parity-tester` with system scope:

```bash
./gradlew test --tests "*Parity*[System]*" --info
```

| Test Category | Runner |
|--------------|--------|
| combat | `CombatParityTests` |
| woodcutting | `WoodcuttingParityTests` |
| mining | `MiningParityTests` |
| fishing | `FishingParityTests` |
| magic | `MagicParityTests` |
| prayer | `PrayerParityTests` |
| economy | `EconomyParityTests` |
| all | All of the above |

## Phase 4: Mechanic Parity Analysis

Spawn `mechanic-parity-analyst` to:
- Score each mechanic: pass/fail/partial
- Identify root causes for failures
- Suggest which agent to assign each failure to

## Phase 5: Compare to Previous Score

```python
# Load previous score from session state
prev = yaml.load("production/session-state/parity-score.yaml")
curr = calculate_score(results)
delta = curr["score"] - prev["score"]
```

## Phase 6: Generate Report

Spawn `qa-parity-verifier` to produce:
- `tests/parity/PARITY-REPORT-[system]-[date].md` (using parity-test-report template)
- Update `production/session-state/parity-score.yaml`

## Phase 7: Deployment Gate Check

If `system == all`:
- Score ≥ 95%: 🟢 Phase 7 approved
- Score ≥ 90%: 🟡 Proceed with warnings
- Score < 90%: 🔴 Block Phase 7

## Error Recovery

| Error | Recovery |
|-------|---------|
| Test compilation failure | Show compiler error; fix before re-running |
| No parity tests for system | Create stub test file; run `/gdd-osrs-specialized-framework` |
| Wiki fetch fails | Use cached wiki data from last successful fetch |

## Nuances

- Floating-point parity tests use tolerance of ±0.001
- Drop rate tests require N ≥ 10,000 simulations to be statistically valid
- Some mechanics cannot be parity-tested without live OSRS comparison (e.g., exact NPC pathfinding)
- Use `--tests "*Regression*"` flag to only run regression tests (faster)

## Next Steps

After parity report:
- For failures: assign to relevant specialist agent
- For score ≥ 90%: proceed to next phase
- For `all` mode with score ≥ 95%: run `/docker-deployment-packager`
