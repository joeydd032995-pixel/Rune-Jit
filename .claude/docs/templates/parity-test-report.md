# Parity Test Report

**Date**: YYYY-MM-DD
**Revision**: [OSRS revision]
**Commit**: [git SHA]
**Run Duration**: [X]s
**Runner**: [regression-suite-runner | qa-parity-verifier | full-parity-tester]

---

## Global Score

| Score | Previous | Delta | Gate |
|-------|---------|-------|------|
| [X]% | [Y]% | [+/-Z]% | 🟢 PASS / 🟡 WARN / 🔴 BLOCK |

---

## System Scores

| System | Tests | Passed | Failed | Score | Trend |
|--------|-------|--------|--------|-------|-------|
| combat | | | | | ↑/↓/→ |
| skilling/woodcutting | | | | | |
| skilling/mining | | | | | |
| skilling/fishing | | | | | |
| skilling/magic | | | | | |
| prayer | | | | | |
| inventory/bank | | | | | |
| economy/shops | | | | | |
| networking | | | | | |
| pathfinding | | | | | |
| **Total** | | | | | |

---

## Regressions (previously passing, now failing)

| Test | System | Expected | Actual | Last Pass Commit |
|------|--------|---------|--------|-----------------|
| | | | | |

_[None — all previously passing tests still pass]_

---

## New Passes (previously failing, now passing)

| Test | System | Value |
|------|--------|-------|
| | | |

---

## Top 10 Failing Tests

| Test | System | Expected | Actual | Delta |
|------|--------|---------|--------|-------|
| | | | | |

---

## Performance Stats

| Metric | Value |
|--------|-------|
| Tick P50 | [X]ms |
| Tick P99 | [X]ms |
| Memory (heap) | [X]MB |

---

## Next Steps

1. Fix [top regression]
2. Investigate [system] score drop
3. Run `/verify-mechanic-parity-1to1 [system]` for detailed analysis
