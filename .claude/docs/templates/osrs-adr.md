# ADR-[NUMBER]: [Decision Title]

**Date**: YYYY-MM-DD
**Status**: PROPOSED | ACCEPTED | SUPERSEDED
**Supersedes**: ADR-[N] (if applicable)
**Superseded by**: ADR-[N] (if applicable)

---

## Context

[Describe the situation that forced this decision. What is the technical problem? What are the constraints?]

**OSRS Revision**: [revision where this applies]

---

## Decision

[State the decision clearly. One or two sentences.]

---

## Formula Source

**Wiki URL**: https://oldschool.runescape.wiki/w/[topic]

```
[Exact formula from wiki — copy it verbatim]
```

**Implementation**:
```kotlin
// Implementation of the above formula
fun [formulaName]([params]): [returnType] {
    // [implementation]
}
```

---

## Alternatives Considered

### Option A: [name]
[Description]
**Rejected because**: [reason]

### Option B: [name]
[Description]
**Rejected because**: [reason]

---

## Implementation Approach

[How this decision is implemented in the codebase. File paths, class names.]

**Files affected**:
- `src/server/[path]`

---

## Test Evidence

| Test | Expected | Actual | Pass? |
|------|---------|--------|-------|
| [test name] | [value from wiki] | [server result] | ✅/❌ |

---

## Parity Impact

**Before this decision**: [X]% parity
**After this decision**: [Y]% parity
**Delta**: [+/-Z]%

---

## Notes

[Any implementation gotchas, future considerations, or links to related ADRs.]
