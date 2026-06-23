# Shared — Protocol Definitions & Shared Types

This module contains protocol definitions, packet specifications, and data types
shared between the client and server.

## Contents

```
src/shared/
├── protocol-defs.yaml      # All packet opcodes, sizes, field definitions
├── revision.yaml           # Pinned target revision (auto-updated by /setup-revision-lock-and-pin)
├── item-types/             # Shared item definition interfaces
├── npc-types/              # Shared NPC definition interfaces
└── README.md               # This file
```

## Protocol Definitions

`protocol-defs.yaml` is the single source of truth for all ~200 game packets.
It is generated and maintained by `/protocol-packet-engine` and should not be
edited by hand.

Format per packet:
```yaml
packets:
  - name: WALK_HERE
    opcode: 98
    direction: client-to-server
    size: 5
    revision: 225
    fields:
      - name: x
        type: SHORT
      - name: y
        type: SHORT
      - name: ctrl
        type: BYTE
```

## Revision Pin

`revision.yaml` is written by `/setup-revision-lock-and-pin`. All components
(client, server, cache loader) read this file to ensure they are targeting
the same OSRS revision.
