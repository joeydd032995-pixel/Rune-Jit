---
name: paoloka-interface-integrator
description: "Integrates the PaoloKa/Interface-tool widget editor, loads the OSRS widget tree from cache, implements CS2 script execution, and wires up the custom overlay system."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /paoloka-interface-integrator

Integrates the OSRS widget interface system and overlay engine.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Cache downloaded (index 3) | Yes |
| Client rendering pipeline working | Yes |
| CS2 script definitions in cache | Yes (index 12) |

## Phase 2: Load Widget Tree from Cache

Spawn `widget-interface-specialist` to:
- Load all interface definitions from cache index 3
- Parse `Widget` data classes for each interface
- Build the interface ID → component tree mapping

Key interfaces to verify load correctly:
- 161 (fixed viewport), 548 (resizable), 12 (bank), 149 (inventory)

## Phase 3: CS2 Script Engine

Spawn `widget-interface-specialist` to implement `src/client/ui/CS2Interpreter.kt`:
- Integer and string stacks
- ~200 CS2 opcodes (SET_TEXT, HIDE, SET_MODEL, etc.)
- Script triggering on widget click/hover events

## Phase 4: PaoloKa Integration

Integrate PaoloKa/Interface-tool for widget inspection:

```bash
# PaoloKa tool setup (tools/interface-tool/)
git clone https://github.com/PaoloKa/Interface-tool tools/interface-tool
# Tool allows: view widget tree, export JSON overlay specs, test layout changes
```

Spawn `widget-interface-specialist` to implement `InterfaceToolBridge.kt`:
- Load JSON overlay files from `tools/interface-tool/exports/`
- Apply position/text overrides to widgets at runtime

## Phase 5: Overlay System

Spawn `ui-overlay-engineer` to implement the RuneLite-compatible overlay API:
- `Overlay` interface
- `OverlayLayer` enum (ABOVE_SCENE, ABOVE_WIDGETS, etc.)
- `OverlayManager` registration and rendering

Implement built-in overlays:
- HP/Prayer/Run/Spec orbs
- XP drops
- Status effects display

## Phase 6: Right-Click Menu

Spawn `input-camera-controller` to wire up:
- `MenuEntry` construction from widget/NPC/object/player actions
- Menu rendering (background sprite + text entries)
- Entry click handling → send to server

## Phase 7: Integration Tests

- Inventory widget renders items correctly
- Bank interface opens/closes on server packet
- Right-click menu appears with correct entries
- XP drop fires on skill XP gain

## Error Recovery

| Error | Recovery |
|-------|---------|
| Widget not found | Log warning; skip render for that component |
| CS2 unknown opcode | Log warning; continue execution (NOPs unknown) |
| PaoloKa tool missing | Skip overlay integration; core widgets still work |

## Nuances

- CS2 scripts run on the client tick (20ms), not the 600ms server tick
- Some widgets are server-driven (bank contents); others are client-driven (prayer selection)
- The fixed/resizable/resizable-modern layouts use different interface IDs
- Overlay draw order must respect `OverlayLayer` to avoid Z-order issues

## Next Steps

1. Run `/implement-client-rendering-pipeline` if not already done
2. Run `/protocol-packet-engine` to connect to server
3. Test by logging in and verifying all widgets display correctly
