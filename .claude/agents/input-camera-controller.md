---
name: input-camera-controller
description: "Implements all player input handling: mouse clicks (left/right), keyboard shortcuts, camera rotation/zoom/pitch, right-click context menus, and OSRS-accurate interaction routing."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Input & Camera Controller

You implement input handling and camera control for the OSRS client.

## Mouse Input

```kotlin
class MouseHandler : MouseListener, MouseMotionListener, MouseWheelListener {
    var lastClickX = 0
    var lastClickY = 0
    var isDragging = false
    var isRightClick = false

    override fun mousePressed(e: MouseEvent) {
        lastClickX = e.x
        lastClickY = e.y
        if (e.button == MouseEvent.BUTTON3) isRightClick = true
    }

    override fun mouseReleased(e: MouseEvent) {
        if (isRightClick && !isDragging) {
            openContextMenu(lastClickX, lastClickY)
        } else if (!isRightClick) {
            processLeftClick(lastClickX, lastClickY)
        }
        isRightClick = false
        isDragging = false
    }

    override fun mouseDragged(e: MouseEvent) {
        isDragging = true
        if (isRightClick) {
            rotateCamera(e.x - lastClickX, e.y - lastClickY)
            lastClickX = e.x
            lastClickY = e.y
        }
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        zoomCamera(e.wheelRotation)
    }
}
```

## Keyboard Shortcuts

```kotlin
enum class KeyShortcut(val keyCode: Int) {
    ESC(27), F1(112), F2(113), F3(114), F4(115), F5(116), F6(117),
    F7(118), F8(119), F9(120), F10(121), F11(122), F12(123),
    COMMA(44), PERIOD(46), UP(38), DOWN(40), LEFT(37), RIGHT(39),
    PLUS(107), MINUS(109)
}

fun handleKeyPress(keyCode: Int) = when (keyCode) {
    KeyShortcut.ESC.keyCode -> closeTopInterface()
    KeyShortcut.F1.keyCode -> openTab(GameTab.COMBAT_OPTIONS)
    KeyShortcut.F2.keyCode -> openTab(GameTab.SKILLS)
    KeyShortcut.F3.keyCode -> openTab(GameTab.QUEST_LIST)
    KeyShortcut.F4.keyCode -> openTab(GameTab.INVENTORY)
    KeyShortcut.F5.keyCode -> openTab(GameTab.EQUIPMENT)
    KeyShortcut.F6.keyCode -> openTab(GameTab.PRAYER)
    KeyShortcut.F7.keyCode -> openTab(GameTab.MAGIC)
    KeyShortcut.F8.keyCode -> openTab(GameTab.CLAN)
    KeyShortcut.F9.keyCode -> openTab(GameTab.ACCOUNT)
    KeyShortcut.F10.keyCode -> openTab(GameTab.FRIENDS)
    KeyShortcut.F11.keyCode -> openTab(GameTab.LOGOUT)
    KeyShortcut.F12.keyCode -> openTab(GameTab.OPTIONS)
    else -> {}
}
```

## Camera System

OSRS camera uses integer arithmetic (no floats):

```kotlin
object Camera {
    var yaw: Int = 0        // 0-2047 (11-bit rotation)
    var pitch: Int = 383    // 128-383 (restricted range)
    var zoom: Int = 700     // 128-3200
    var x: Int = 0          // world X (tile * 128)
    var y: Int = 0          // world Z (tile * 128)

    const val PITCH_MIN = 128
    const val PITCH_MAX = 383
    const val ZOOM_MIN = 128
    const val ZOOM_MAX = 3200

    fun rotateDelta(deltaYaw: Int, deltaPitch: Int) {
        yaw = (yaw + deltaYaw) and 2047
        pitch = (pitch + deltaPitch).coerceIn(PITCH_MIN, PITCH_MAX)
    }

    fun zoom(delta: Int) {
        zoom = (zoom + delta * 30).coerceIn(ZOOM_MIN, ZOOM_MAX)
    }
}
```

## Click Routing

Left click routes based on what is at screen position (x,y):
1. Check widget hit first (UI elements take priority)
2. If no widget: check minimap
3. If no minimap: project screen → world ray; find first object/NPC/player/tile

```kotlin
fun processLeftClick(screenX: Int, screenY: Int) {
    widgetManager.findAt(screenX, screenY)?.let {
        return handleWidgetClick(it, screenX, screenY)
    }
    sceneRaycaster.pick(screenX, screenY)?.let { entity ->
        sendInteractPacket(entity, getDefaultAction(entity))
    }
}
```

## Context Menu

Right-click builds menu entries from all entities under cursor, sorted:
NPC actions → Object actions → Ground item actions → Walk here → Cancel
