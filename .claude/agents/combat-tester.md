---
name: combat-tester
description: "Tests all combat formulas against OSRS wiki values: accuracy, max hit, DPS calculations, prayer bonuses, special attacks, and multiway combat rules."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Combat Tester

You validate all combat mechanics against OSRS wiki formulas.

## Max Hit Tests

```kotlin
class MaxHitTests {

    @Test fun testMeleeMaxHit() {
        // Wiki: floor(0.5 + effective_strength * (strength_bonus + 64) / 640)
        // At 99 str, berserker ring, dragon dagger: effective_str = 118, str_bonus = 83
        val result = CombatFormulas.meleeMaxHit(effectiveStrength = 118, strengthBonus = 83)
        assert(result == 25) {
            "Expected 25, got $result. See: https://oldschool.runescape.wiki/w/Maximum_melee_hit"
        }
    }

    @Test fun testDharokMaxHit() {
        // Full Dharok at 1 HP: max_hit *= (1 + (99 - current_hp) / 100)
        val baseMax = CombatFormulas.meleeMaxHit(effectiveStrength = 118, strengthBonus = 102)
        val dharokMax = DharokSetEffect.applyBonus(baseMax, currentHp = 1, maxHp = 99)
        assert(dharokMax >= 120) {
            "Dharok at 1 HP should hit 120+. See: https://oldschool.runescape.wiki/w/Dharok%27s_armour_set_effect"
        }
    }

    @Test fun testVoidMeleeBonus() {
        // Void melee: +10% effective attack and strength
        val withVoid = CombatFormulas.effectiveStrength(
            strengthLevel = 99, prayerBonus = 1.0, voidBonus = 1.1
        )
        val withoutVoid = CombatFormulas.effectiveStrength(
            strengthLevel = 99, prayerBonus = 1.0, voidBonus = 1.0
        )
        assert(withVoid > withoutVoid) { "Void should increase effective strength" }
        assert(abs((withVoid.toDouble() / withoutVoid) - 1.1) < 0.05)
    }
}
```

## Accuracy Tests

```kotlin
class AccuracyTests {

    @Test fun testMeleeAccuracy() {
        // Attack roll = effective_attack * (equipment_bonus + 64)
        // Defence roll = effective_defence * (defence_bonus + 64)
        // Accuracy = if atk > def: 1 - (def + 2) / (2 * (atk + 1))
        //            if atk <= def: atk / (2 * (def + 1))
        val attackRoll = 10000
        val defenceRoll = 5000
        val accuracy = CombatFormulas.accuracy(attackRoll, defenceRoll)
        val expected = 1.0 - (defenceRoll + 2) / (2.0 * (attackRoll + 1))
        assert(abs(accuracy - expected) < 0.001)
    }
}
```

## Prayer Bonus Tests

```kotlin
class PrayerBonusTests {

    @Test fun testPieytyStrengthBonus() {
        // Piety: +23% strength bonus
        val piety = Prayers.PIETY
        assert(abs(piety.strengthBonus - 1.23) < 0.001) {
            "Piety should give +23% strength. See: https://oldschool.runescape.wiki/w/Piety"
        }
    }

    @Test fun testOverheadProtection() {
        // Protect from Melee: reduces damage in PvM (wiki says 0% for NPCs hitting protected prayer)
        // Actually in OSRS: protection prayers do NOT block NPC attacks (common misconception)
        // They only reduce from other players
        val player = TestPlayer(activePrayers = setOf(Prayer.PROTECT_MELEE))
        val npc = TestNpc(NpcIds.ABYSSAL_DEMON)
        val hit = CombatEngine.calculateHit(npc, player)
        // NPC can still deal full damage despite prayer (for PvM)
        assert(hit.maxPossibleDamage == CombatFormulas.npcMaxHit(npc))
    }
}
```

## Special Attack Tests

```kotlin
class SpecialAttackTests {

    @Test fun testDragonDaggerSpec() {
        // DDS spec: 2 hits, each with 25% bonus max hit
        val player = TestPlayer(weapon = ItemIds.DRAGON_DAGGER, specialEnergy = 1000)
        val target = TestNpc(NpcIds.COW)
        val results = player.useSpecialAttack(target)

        assert(results.size == 2) { "DDS should hit twice" }
        results.forEach { hit ->
            assert(hit.maxPossibleDamage <= CombatFormulas.meleeMaxHit(player) * 1.25 + 1)
        }
    }

    @Test fun testAncestralSpecEnergyDrain() {
        // Each special attack consumes energy
        val player = TestPlayer(specialEnergy = 1000)
        player.useSpecialAttack(TestNpc(NpcIds.COW))
        assert(player.specialEnergy == 750) { "DDS should consume 25% special (250/1000)" }
    }
}
```

## DPS Simulation

Runs 10,000 simulated attacks to verify average DPS within 1% of wiki value.
