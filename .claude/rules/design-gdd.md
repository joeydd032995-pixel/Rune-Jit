---
path: design/gdd/**
---

# Design GDD Rules

## Required Sections

Every GDD in `design/gdd/` must contain these 10 sections:

1. **Mechanic Overview** — 2-3 sentence description of the system
2. **XP/Reward Formula** — exact formula with wiki source URL
3. **Content List** — complete table of all items/actions/locations
4. **Level Requirements** — minimum levels for each action
5. **Required Items/Tools** — equipment needed
6. **Tick Rate** — game ticks per action
7. **Special Mechanics** — pet drops, random events, bonus XP
8. **Parity Target** — target parity score for this system (e.g., "95%")
9. **Edge Cases** — known tricky behaviors (tick manipulation, instancing, etc.)
10. **Wiki Citation** — primary OSRS wiki page URL for this system

```markdown
# Woodcutting GDD

## 1. Mechanic Overview
...

## 2. XP Formula
`XP = tree.xpPerLog` (no formula variation; flat XP per tree type)
Source: https://oldschool.runescape.wiki/w/Woodcutting#Experience

## 3. Content List
| Tree | Level | XP | Log | Tool |
|------|-------|----|-----|------|
| Normal | 1 | 25 | 1511 | Any axe |
...

## 10. Wiki Citation
https://oldschool.runescape.wiki/w/Woodcutting
```

## Parity Checklist

Each GDD must be accompanied by `${name}-parity.md` in `tests/parity/`:
- One row per wiki-documented value
- Status column: ✅ implemented / ❌ missing / ⚠️ wrong

## Prohibited

- No GDD without all 10 sections
- No values in GDD that don't have wiki citations
- No "TODO: verify" in a finalized GDD (mark as DRAFT if unverified)
