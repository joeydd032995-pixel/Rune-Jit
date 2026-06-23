---
path: src/server/skills/**
---

# Server Skills Rules

## Data-Driven Requirement

**All skill values must be loaded from data files at startup** — never hardcode XP amounts, level requirements, item IDs, or tick rates in skill logic.

```kotlin
// ❌ WRONG: Hardcoded values
fun chopTree(player: Player, treeType: TreeType) {
    if (player.skills.getLevel(Skill.WOODCUTTING) < 75) return  // hardcoded
    player.skills.addXp(Skill.WOODCUTTING, 250.0)  // hardcoded
}

// ✅ CORRECT: Data-driven
fun chopTree(player: Player, treeType: TreeType) {
    val def = WoodcuttingData.trees[treeType] ?: return
    if (player.skills.getLevel(Skill.WOODCUTTING) < def.levelRequired) return
    player.skills.addXp(Skill.WOODCUTTING, def.xp)
}
```

## XP Formula Sources

Every XP value must have a corresponding wiki citation in the data file:

```yaml
# data/skills/woodcutting.yaml
trees:
  MAGIC:
    level_required: 75
    xp: 250.0
    wiki: "https://oldschool.runescape.wiki/w/Woodcutting#Experience"
    log_item_id: 1513
    chop_animation: 713
    success_rate_formula: "floor(random(255) < floor(level * 2 + axe_bonus) / tree_difficulty)"
```

## Tick Rate Accuracy

Skill actions run on exact tick intervals matching OSRS:
- **Woodcutting**: 4 ticks per attempt (2.4s)
- **Mining**: 3-8 ticks depending on rock (varies by ore)
- **Fishing**: 5 ticks per attempt (3s)
- **Cooking**: 4 ticks per food item

## Plugin Architecture

Each skill is an rsmod plugin in its own package:
```
src/server/skills/
├── woodcutting/
│   ├── WoodcuttingPlugin.kt    # skill registration
│   ├── WoodcuttingAction.kt    # action handler
│   └── WoodcuttingData.kt      # data classes
└── mining/
    └── ...
```

## Prohibited

- No hardcoded item IDs (use `ItemIds.kt` constants loaded from data)
- No hardcoded level requirements in action logic (use data file)
- No `Thread.sleep` or blocking I/O in skill handlers
