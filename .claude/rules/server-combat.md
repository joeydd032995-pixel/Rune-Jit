---
path: src/server/combat/**
---

# Server Combat Rules

## Formula Accuracy

**All combat formulas must exactly match the OSRS wiki** — no approximations without an ADR documenting the exact delta and justification.

Required wiki citations for all formulas:
- Max hit: https://oldschool.runescape.wiki/w/Maximum_melee_hit
- Accuracy: https://oldschool.runescape.wiki/w/Accuracy
- Prayer bonuses: https://oldschool.runescape.wiki/w/Prayer#Bonuses

## Integer Arithmetic

OSRS combat uses integer arithmetic with `floor()` at specific points. Do not use floating-point intermediate results:

```kotlin
// ❌ WRONG: Float arithmetic loses precision
fun maxHit(effectiveStrength: Int, strengthBonus: Int): Int {
    return (0.5 + effectiveStrength * (strengthBonus + 64) / 640.0).toInt()
}

// ✅ CORRECT: Integer arithmetic with floor at end
fun maxHit(effectiveStrength: Int, strengthBonus: Int): Int {
    // floor(0.5 + eff_str * (str_bonus + 64) / 640)
    return ((effectiveStrength.toLong() * (strengthBonus + 64) + 320) / 640).toInt()
}
```

## Prayer Multiplicative Stacking

Prayer bonuses apply multiplicatively, not additively:

```kotlin
// ❌ WRONG: Additive
val strengthBonus = 1.0 + pieytyBonus + rigourBonus  // wrong

// ✅ CORRECT: Multiplicative
val strengthBonus = pieytyBonus * rigourBonus  // never stack two offensive prayers anyway
```

## Special Attacks

Each weapon's special attack behavior must match wiki specification exactly:
- DDS: 2 hits, ×1.25 max hit, 25% spec cost
- Dragon claws: 4-hit cascade, specific damage rules
- Saradomin sword: +16 magic damage, 55% spec cost

## Combat Tick Order

Within a combat tick:
1. Check for death (HP ≤ 0)
2. Apply poison/venom damage
3. Apply regeneration (+1 HP per 60 ticks)
4. Process queued attacks (melee/ranged/magic)
5. Drain prayer

## Prohibited

- No clamping of damage that OSRS doesn't clamp
- No "protection prayer = 0 damage from NPCs" (common misconception; protection reduces PvP only)
- No client-side hit validation
