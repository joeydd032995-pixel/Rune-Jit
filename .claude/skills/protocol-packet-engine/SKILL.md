---
name: protocol-packet-engine
description: "Implements the complete OSRS packet protocol: ISAAC cipher, login handshake, JS5 cache server, and all ~200 game packet handlers. References RuneStar leak data and reverse-engineered mappings."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: opus
---

# /protocol-packet-engine

Implements the full OSRS client-server packet protocol.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Revision pinned | Yes |
| `src/shared/mappings.yaml` exists | Yes (from /setup-revision-lock-and-pin) |
| Cache downloaded (for JS5) | Yes |
| RuneStar leak data referenced | Yes |

## Phase 2: Reverse Engineer Packet Opcodes

Spawn `reverse-engineer` to:
- Identify all packet handlers in gamepack from mappings.yaml
- Cross-reference with RuneStar leak data for names
- Produce `src/shared/protocol-defs.yaml` with all ~200 packets

## Phase 3: Implement ISAAC Cipher

Spawn `isaac-cipher-handler` to implement `src/server/net/IsaacCipher.kt`:
- Full ISAAC PRNG implementation
- Server/client seed pair from login block
- Encode cipher (server→client): keys + 50
- Decode cipher (client→server): keys as-is

## Phase 4: Implement Login Sequence

Spawn `client-network-handler` to implement `src/server/net/LoginHandler.kt`:
- Login type detection (16=game, 18=reconnect)
- RSA-encrypted login block decryption
- Username/password validation
- Session key extraction
- Login response codes (0-11)

## Phase 5: Implement JS5 Cache Server

Spawn `js5-cache-server` to implement `src/server/net/JS5Server.kt`:
- Client connects on port 43595
- Serves cache index files (0-21 + 255)
- Priority queue (high-priority files served first)
- Checksums validated against index 255

## Phase 6: Implement Game Packet Handlers

Spawn `protocol-sync-validator` to:
- Register all ~200 packet handlers
- Validate each handler against `protocol-defs.yaml`
- Flag any missing or size-mismatched handlers

Priority packets to implement first:
1. `WALK` — player movement
2. `WIDGET_ACTION` — interface interactions
3. `OBJECT_INTERACT` — click on world objects
4. `NPC_INTERACT` — click on NPCs
5. `GROUND_ITEM_INTERACT` — pick up items
6. `CHAT` — public/private chat

## Phase 7: Validate Coverage

```bash
./gradlew test --tests "*ProtocolParity*"
```

Target: 95%+ packet coverage (all packets have handlers registered).

## Error Recovery

| Error | Recovery |
|-------|---------|
| Opcode mismatch with client | Re-run reverse-engineer; update protocol-defs.yaml |
| ISAAC cipher desync | Verify seed key handshake during login |
| JS5 connection refused | Check port 43595 not blocked; verify cache presence |
| Login response 6 (revision mismatch) | Verify revision.yaml matches gamepack version |

## Nuances

- OSRS uses RSA-2048 for the inner login block; private server needs its own keypair
- JS5 sends files in binary chunks; client reassembles them
- Packet opcodes change with each OSRS revision — pin revision before running this skill
- The `gamecycle` counter in some packets is 1-indexed (client sends current tick)
- VAR_BYTE packets have a 1-byte length prefix; VAR_SHORT have a 2-byte prefix

## Next Steps

1. Run `/implement-client-rendering-pipeline` (client side)
2. Run `/verify-mechanic-parity-1to1 networking` (packet coverage test)
3. Run full integration test: log in with client, verify world loads
