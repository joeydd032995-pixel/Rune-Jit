---
name: ui-overlay-engineer
description: "Implements the OSRS HUD overlays: skill orbs (HP/Prayer/Run/Special), XP drops, combat level display, status effects display, and all game status indicators rendered over the main viewport."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# UI Overlay Engineer

You implement the OSRS HUD and overlay system drawn over the game viewport.

## Core HUD Elements

```kotlin
class OsrsHud : Overlay {
    override val layer = OverlayLayer.ABOVE_SCENE

    override fun render(g: Graphics2D): Dimension {
        drawHpOrb(g)
        drawPrayerOrb(g)
        drawRunOrb(g)
        drawSpecialAttackOrb(g)
        drawCompassWidget(g)
        return Dimension(512, 334)  // fixed mode viewport
    }
}
```

## HP Orb

```kotlin
fun drawHpOrb(g: Graphics2D) {
    val hp = client.localPlayer.hitpoints
    val maxHp = client.localPlayer.maxHitpoints
    val percent = hp.toFloat() / maxHp.toFloat()

    // Draw orb sprite (filled arc based on percentage)
    SpriteRenderer.draw(g, OrbSprites.HP_BACKGROUND, orbX, orbY)

    g.color = when {
        percent > 0.5 -> Color(0, 255, 0)      // green
        percent > 0.25 -> Color(255, 165, 0)    // orange
        else -> Color(255, 0, 0)                // red
    }
    g.fillArc(orbX + 3, orbY + 3, 26, 26, 90, -(360 * percent).toInt())
    SpriteRenderer.draw(g, OrbSprites.HP_FOREGROUND, orbX, orbY)

    // Draw number
    g.color = Color.WHITE
    g.font = OSRS_FONT_SMALL
    g.drawString(hp.toString(), orbX + 32, orbY + 16)
}
```

## Prayer Orb

```kotlin
fun drawPrayerOrb(g: Graphics2D) {
    val prayer = client.localPlayer.prayer
    val maxPrayer = client.localPlayer.maxPrayer
    val percent = prayer.toFloat() / maxPrayer.toFloat()
    val isQuickPrayer = client.localPlayer.quickPrayerActive

    val orbColor = if (isQuickPrayer) Color.CYAN else Color(0, 200, 255)
    SpriteRenderer.draw(g, OrbSprites.PRAYER_BACKGROUND, prayerOrbX, prayerOrbY)
    g.color = orbColor
    g.fillArc(prayerOrbX + 3, prayerOrbY + 3, 26, 26, 90, -(360 * percent).toInt())
    SpriteRenderer.draw(g, OrbSprites.PRAYER_FOREGROUND, prayerOrbX, prayerOrbY)
    g.color = Color.WHITE
    g.drawString(prayer.toString(), prayerOrbX + 32, prayerOrbY + 16)
}
```

## Run Energy Orb

```kotlin
fun drawRunOrb(g: Graphics2D) {
    val runEnergy = client.localPlayer.runEnergy  // 0-10000 (displayed as 0-100%)
    val isRunning = client.localPlayer.isRunning
    val percent = runEnergy / 10000f

    val color = if (isRunning) Color.YELLOW else Color(200, 200, 200)
    SpriteRenderer.draw(g, OrbSprites.RUN_BACKGROUND, runOrbX, runOrbY)
    g.color = color
    g.fillArc(runOrbX + 3, runOrbY + 3, 26, 26, 90, -(360 * percent).toInt())
    SpriteRenderer.draw(g, OrbSprites.RUN_FOREGROUND, runOrbX, runOrbY)
    g.color = Color.WHITE
    g.drawString("${(runEnergy / 100)}%", runOrbX + 32, runOrbY + 16)
}
```

## Special Attack Bar

```kotlin
fun drawSpecialAttackOrb(g: Graphics2D) {
    val special = client.localPlayer.specialAttackEnergy  // 0-1000 (10 = 1%)
    val isSpecialActive = client.localPlayer.specialAttackActive
    val percent = special / 1000f

    val color = if (isSpecialActive) Color.GREEN else Color(200, 255, 200)
    // Draw segmented bar (10 segments of 10% each)
    for (i in 0 until 10) {
        val filled = (percent * 10) >= (i + 1)
        g.color = if (filled) color else Color.DARK_GRAY
        g.fillRect(specBarX + i * 10, specBarY, 9, 6)
    }
}
```

## XP Drop Overlay

```kotlin
class XpDropOverlay : Overlay {
    private val drops = ArrayDeque<XpDrop>()
    data class XpDrop(val skill: Skill, val xp: Int, var y: Float, val createdAt: Long)

    fun addDrop(skill: Skill, xpGained: Int) {
        drops.addFirst(XpDrop(skill, xpGained, 0f, System.currentTimeMillis()))
    }

    override fun render(g: Graphics2D): Dimension {
        val now = System.currentTimeMillis()
        drops.removeAll { now - it.createdAt > 3000 }  // expire after 3s
        drops.forEachIndexed { i, drop ->
            drop.y = i * 16f  // stack upward
            g.color = skillColor(drop.skill)
            g.drawString("+${drop.xp}", xpDropX, (xpDropY + drop.y).toInt())
        }
        return Dimension.ZERO
    }
}
```

## Status Effects Display

Displays active poison, venom, disease, freeze timers via colored text overlays above the HP orb.
