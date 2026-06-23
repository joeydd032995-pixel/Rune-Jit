# NPC Behavior Spec: [NPC NAME]

**NPC ID**: [id]
**Combat Level**: [level]
**Type**: Passive | Aggressive | Boss | Quest NPC
**Location**: [region/area]
**Wiki**: https://oldschool.runescape.wiki/w/[NPC_Name]

---

## Basic Stats

| Stat | Value | Source |
|------|-------|--------|
| Hitpoints | | wiki |
| Attack | | |
| Strength | | |
| Defence | | |
| Magic | | |
| Ranged | | |
| Max Hit | | |
| Attack Speed | [X] ticks | |

---

## Combat Style

| Style | Bonus | Notes |
|-------|-------|-------|
| [Melee/Ranged/Magic] | | |

---

## Aggression

| Property | Value |
|----------|-------|
| Aggressive | Yes/No |
| Aggression Range | [X] tiles |
| Aggro Condition | Always / Only if level ≤ X / Quest state |
| De-aggro Timer | [X] minutes (standard: 10 min) |

---

## Pathing Behavior

| Behavior | Description |
|----------|-------------|
| Wander radius | [X] tiles from spawn |
| Follow distance | [X] tiles max follow |
| Respawn time | [X] ticks (~[Y]s) |
| Multi-combat | Yes/No |

---

## Special Attacks (if applicable)

| Attack | Trigger Condition | Damage | Effect | Frequency |
|--------|------------------|--------|--------|-----------|
| [name] | [condition] | [damage] | [effect] | Every [X] ticks |

---

## Drop Table

| Item | Quantity | Rate | Source |
|------|----------|------|--------|
| Always: | | | |
| Common: | | | |
| Rare: | | 1/[X] | [wiki URL] |
| Unique: | | 1/[X] | |

---

## Dialogue (if quest NPC)

| Trigger | Dialogue Key | Notes |
|---------|-------------|-------|
| Default | [key] | |
| Quest state [X] | [key] | |

---

## Implementation Notes

[Special behaviors, server-side scripting notes, known OSRS quirks for this NPC]
