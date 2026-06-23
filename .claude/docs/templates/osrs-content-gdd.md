# [CONTENT NAME] GDD — [Quest/Minigame/Boss]

**Type**: Quest | Minigame | Boss | Raid | Area
**Status**: DRAFT | REVIEW | FINAL
**Quest Points**: [X] (if applicable)
**Wiki**: https://oldschool.runescape.wiki/w/[Content_Name]

---

## 1. Overview

[2-3 sentences describing the content and why players engage with it.]

---

## 2. Requirements

### To Access
| Requirement | Value | Type |
|-------------|-------|------|
| Quest Points | X | Minimum |
| [Skill] | [Level] | Level requirement |
| [Quest] | Complete | Quest requirement |

### Recommended
| Item/Level | Purpose |
|-----------|---------|

---

## 3. Mechanics

### Core Mechanic
[How the main gameplay loop works]

### State Machine (for quests/bosses with phases)
```
State 0: [initial state]
  → Trigger: [what progresses state]
State 1: [next state]
  → Trigger: ...
State COMPLETE: [final state]
```

### Boss Mechanics (if applicable)
| Phase | HP Threshold | Special Attacks | Pattern |
|-------|-------------|-----------------|---------|
| 1 | 100% → 75% | | |

---

## 4. Rewards

### Quest Rewards (if applicable)
| Reward | Amount |
|--------|--------|
| Quest Points | X |
| [Skill] XP | X |
| Items | [list] |

### Boss Drops (if applicable)
| Item | Base Rate | Source |
|------|-----------|--------|
| | 1/[X] | [wiki URL] |

---

## 5. Dialogue Outline (for quests)

```
NPC: [greeting dialogue]
→ Player Option 1: [...]
  → NPC: [response]
→ Player Option 2: [...]
```

---

## 6. Variable States

| Varbit ID | Value | Meaning |
|-----------|-------|---------|
| [ID] | 0 | Not started |
| [ID] | 1 | [state] |
| [ID] | [final] | Complete |

---

## 7. Edge Cases

| Situation | Expected Behavior |
|-----------|-------------------|
| Player disconnects mid-boss | [behavior] |
| Player dies | [behavior] |

---

## 8. Wiki Citation

https://oldschool.runescape.wiki/w/[Content_Name]
