---
path: src/client/ui/**
---

# Client UI Rules

## No Game State Ownership in UI

UI components must not own or cache game state. All widget data comes from server packets:

```kotlin
// ❌ WRONG: UI caches game state
class InventoryWidget {
    val items = arrayOfNulls<ItemStack>(28)  // DO NOT cache inventory in UI

    fun onItemPickup(item: ItemStack, slot: Int) {
        items[slot] = item  // UI maintains its own state — breaks with server resync
    }
}

// ✅ CORRECT: UI reads from client model (which is populated by server packets)
class InventoryWidget {
    override fun render(g: Graphics2D) {
        ClientModel.inventory.items.forEachIndexed { slot, item ->
            if (item != null) drawItem(g, item, slotX(slot), slotY(slot))
        }
    }
}
```

## Widget Data from Server Packets

All interface content is driven by packets from the server:
- `IF_SET_TEXT` packet → widget text
- `IF_SET_HIDE` packet → widget visibility
- `IF_SET_MODEL` packet → model in widget
- `IF_SET_NPCHEAD` packet → NPC head in widget

Never generate widget content client-side based on derived state.

## Right-Click Menu Consistency

Right-click menu entries come from the server (`SET_MENU_ENTRY` packet). The client may add overlay entries (e.g., "Examine" from cache), but never modify or remove server-provided entries.

## Overlay Layers

Overlays must respect the layer system:
- `ABOVE_SCENE`: drawn over 3D world, under widgets (e.g., entity highlights)
- `ABOVE_WIDGETS`: drawn over everything (e.g., tutorial arrows)
- `UNDER_WIDGETS`: drawn under widget panels

Never draw directly to the canvas bypassing the overlay system.

## Input Validation

Widget interactions must be validated before sending to server:
- Slot index must be within valid range
- Item ID must match what the server last sent for that slot
- Quantity must be positive

## Prohibited

- No UI components maintaining their own copies of inventory/bank/equipment
- No widget scripts executing game logic (CS2 scripts render only)
- No hardcoded interface IDs in new UI code (use `InterfaceIds.kt` constants)
