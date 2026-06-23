# Rune-Jit — OSRS 1:1 Emulator Studio

## What This Repository Is

Rune-Jit is a production-grade, hyper-specialized **AI-powered development studio** for building a pixel-accurate, mechanically identical private OSRS emulator. It orchestrates 84 specialized agents across 8 development phases using 25 slash-command skills, automated validation hooks, and path-scoped coding rules.

**Legal Notice**: This is a private, educational emulator project. No Jagex intellectual property (cache files, game assets, gamepack JARs, XTEA keys) is committed to this repository or distributed. All cache data is fetched by users at runtime from the OpenRS2 Archive (archive.openrs2.org). This project references only open-source reverse-engineering resources.

---

## Target Stack

| Component | Technology |
|-----------|-----------|
| Game Server | Kotlin + rsmod (plugin architecture) |
| Client | RuneLite fork (Java + OpenGL/LWJGL) |
| Networking | Netty (NIO, ISAAC cipher) |
| Build System | Gradle 8 (Kotlin DSL) |
| Java Version | Java 17 (ZGC, JFR profiling) |
| Cache Format | OSRS binary cache (OpenRS2 acquisition) |
| Data Layer | osrsbox-db (JSON, 23k+ items, 5k+ monsters) |
| Protocol | OSRS protocol (JS5 + game packets, ISAAC encrypted) |

---

## 8-Phase Development Workflow

See `.claude/docs/workflow-catalog.yaml` for complete phase definitions.

| Phase | Name | Key Skills |
|-------|------|-----------|
| 0 | Environment & Tooling | `/setup-revision-lock-and-pin`, `/load-osrs-cache-full`, `/import-osrsbox-complete` |
| 1 | Research, GDD & Design | `/gdd-osrs-specialized-framework`, `/documentation-auto-generator` |
| 2 | Server Core | `/implement-tick-engine-core`, `/implement-skill-action-framework`, `/combat-engine-full`, `/protocol-packet-engine` |
| 3 | Client Frontend | `/implement-client-rendering-pipeline`, `/paoloka-interface-integrator` |
| 4 | Integration & Networking | `/protocol-packet-engine`, `/automated-parity-testing-suite` |
| 5 | Content Population | `/raids-and-instanced-content-generator`, `/economy-and-ge-simulator`, `/npc-drop-table-importer`, `/construction-room-builder` |
| 6 | Testing & Iteration | `/automated-parity-testing-suite`, `/performance-profiling-and-optimization`, `/verify-mechanic-parity-1to1` |
| 7 | Deployment | `/docker-deployment-packager`, `/documentation-auto-generator`, `/revision-update-adapter` |

**Orchestrate the full pipeline**: `/full-team-orchestration-workflow [start_phase]`

---

## Quick-Start Skills

```bash
# Phase 0: Bootstrap environment
/setup-revision-lock-and-pin 225
/load-osrs-cache-full
/import-osrsbox-complete

# Phase 2: Implement server core
/implement-tick-engine-core
/implement-skill-action-framework woodcutting
/combat-engine-full
/pathfinding-and-clipping-engine
/protocol-packet-engine

# Phase 6: Measure parity
/verify-mechanic-parity-1to1 combat
/automated-parity-testing-suite all
/performance-profiling-and-optimization 500

# Phase 7: Deploy
/docker-deployment-packager
```

---

## All 25 Skills

| # | Skill | Description |
|---|-------|-------------|
| 01 | `/load-osrs-cache-full` | Download OSRS cache + XTEA keys from OpenRS2 |
| 02 | `/import-osrsbox-complete` | Import 23k+ items, 5k+ monsters, prayers into data layer |
| 03 | `/verify-mechanic-parity-1to1` | Run wiki-based parity tests for any system |
| 04 | `/setup-revision-lock-and-pin` | Pin exact OSRS revision; record gamepack mappings |
| 05 | `/implement-tick-engine-core` | Build 600ms tick loop with exact entity update order |
| 06 | `/implement-skill-action-framework` | Implement any OSRS skill (woodcutting, mining, etc.) |
| 07 | `/combat-engine-full` | Full combat: melee/ranged/magic, prayers, specials |
| 08 | `/pathfinding-and-clipping-engine` | BFS pathfinding from cache clip flags |
| 09 | `/quest-and-diary-state-machine` | Quest engine, dialogue trees, achievement diaries |
| 10 | `/implement-client-rendering-pipeline` | OpenGL renderer, models, animations |
| 11 | `/paoloka-interface-integrator` | Widget tree, CS2 scripts, PaoloKa bridge |
| 12 | `/protocol-packet-engine` | ISAAC cipher, JS5 server, 95%+ packet coverage |
| 13 | `/economy-and-ge-simulator` | Grand Exchange, shop pricing, inflation simulation |
| 14 | `/cache-unpack-extract-assets` | Extract sprites/models/maps/sounds to disk |
| 15 | `/gdd-osrs-specialized-framework` | Create GDDs with wiki citations |
| 16 | `/performance-profiling-and-optimization` | JFR profiling, <580ms P99 tick target |
| 17 | `/raids-and-instanced-content-generator` | CoX, ToB, ToA with instanced regions |
| 18 | `/automated-parity-testing-suite` | Full parity regression suite with deployment gate |
| 19 | `/revision-update-adapter` | Adapt to new OSRS revision, diff mappings |
| 20 | `/full-team-orchestration-workflow` | Orchestrate all 84 agents across all 8 phases |
| 21 | `/npc-drop-table-importer` | Import NPC drop tables from osrsbox |
| 22 | `/prayer-effect-simulator` | All three prayer books, drain rates, protection |
| 23 | `/construction-room-builder` | Player-owned houses, furniture, servants, dungeons |
| 24 | `/docker-deployment-packager` | Docker containers, release bundle, health checks |
| 25 | `/documentation-auto-generator` | ADRs from @annotations, GDD updates, API reference |

---

## Agent Roster (84 Agents)

All agents are in `.claude/agents/`. See `.claude/docs/workflow-catalog.yaml` for phase assignments.

**Phase 0 — Environment (12)**: environment-architect, repo-initializer, cache-downloader, mcp-integrator, revision-pin-specialist, legal-compliance-checker, openrs2-specialist, xtea-key-manager, gamepack-analyst, dependency-validator, devtools-installer, workspace-configurator

**Phase 1 — Research (15)**: osrs-systems-mapper, mechanic-parity-analyst, gdd-author, edge-case-forecaster, quest-state-designer, economy-balancer, data-schema-engineer, wiki-researcher, formula-mathematician, content-cataloger, revision-historian, api-contract-designer, performance-planner, skill-decomposer, combat-theorist

**Phase 2 — Server (18)**: tick-engine-programmer, woodcutting-specialist, mining-specialist, fishing-specialist, magic-spell-caster, combat-engine-architect, prayer-system-architect, npc-behavior-simulator, persistence-layer-expert, js5-cache-server, isaac-cipher-handler, world-region-loader, pathfinding-engineer, item-system-engineer, inventory-system-engineer, shop-system-engineer, quest-engine-programmer, bank-system-engineer

**Phase 3 — Client (14)**: rendering-pipeline-engineer, widget-interface-specialist, input-camera-controller, audio-sfx-integrator, gamepack-loader, hd-graphics-handler, model-renderer, sprite-renderer, minimap-renderer, ui-overlay-engineer, animation-system-engineer, particle-system-engineer, camera-system-engineer, client-network-handler

**Phase 4-5 — Integration/QA (20)**: protocol-sync-validator, full-parity-tester, performance-profiler, load-tester, bug-triager, release-packager, docker-deployer, revision-update-adapter, anti-cheat-patcher, documentation-generator, integration-tester, regression-suite-runner, desync-investigator, memory-profiler, network-analyst, content-parity-verifier, edge-case-analyzer, raid-tester, economy-tester, combat-tester

**Cross-Phase (5)**: reverse-engineer, cache-unpacker, content-importer, qa-parity-verifier, orchestration-conductor

---

## Parity Targets

| System | Target | Deployment Gate |
|--------|--------|----------------|
| Combat | ≥95% | Hard requirement |
| Networking | ≥95% | Hard requirement |
| Skills | ≥90% | Hard requirement |
| Instancing | ≥90% | Required for raids |
| Client | ≥85% | Soft target |
| Economy | ≥80% | Soft target |
| **Global Weighted** | **≥90%** | **Required to deploy** |

---

## Performance Budgets

| Metric | Target | Measured By |
|--------|--------|------------|
| P99 tick latency | <580ms | `/performance-profiling-and-optimization` |
| P99 tick at 500 players | <580ms | load-tester agent |
| Heap memory at 500 players | <4GB | memory-profiler agent |
| GC pause (ZGC) | <10ms | JFR GarbageCollection events |
| Login latency | <2s | integration-tester |

---

## Critical Rules

- **NO Thread.sleep() in tick engine** — all scheduling via `HashedWheelTimer`
- **BFS pathfinding, NOT A*** — A* produces wrong paths vs OSRS (parity failure)
- **ISAAC cipher is mandatory** — never disable; server-authoritative seed exchange
- **Overhead prayers in PvM = 100% block** — in PvP = ~40% reduction
- **All XP values must be exact** — even 0.1 XP difference = parity failure
- **Packet opcodes from protocol-defs.yaml** — never hardcoded; change each revision
- **Entity update order**: players → NPCs → player update blocks → NPC update blocks → ground items → objects
- **Cache NEVER committed** — not even a single .dat2 or .idx file

---

## Repository Structure

```
Rune-Jit/
├── CLAUDE.md                          # This file
├── CONTRIBUTING.md                    # Legal framing, contribution guide
├── .gitignore                         # Protects cache/keys/JARs from commit
├── .claude/
│   ├── settings.json                  # Hooks, permissions, MCP config
│   ├── statusline.sh                  # Phase/revision/parity status display
│   ├── agents/                        # 84 agent definitions
│   ├── skills/                        # 25 skill directories
│   ├── hooks/                         # 8 validation hooks
│   ├── rules/                         # 10 path-scoped coding rules
│   └── docs/
│       ├── workflow-catalog.yaml      # 8-phase pipeline
│       ├── revision.yaml              # Pinned OSRS revision (created by skill)
│       └── templates/                 # 10 GDD/ADR/parity templates
├── src/
│   ├── server/                        # rsmod-kotlin game server
│   ├── client/                        # RuneLite fork
│   └── shared/                        # Protocol definitions
├── cache/                             # .gitignored — run /load-osrs-cache-full
├── data/
│   ├── osrsbox/                       # .gitignored — run /import-osrsbox-complete
│   ├── drops/                         # NPC drop tables (generated by /npc-drop-table-importer)
│   └── construction/                  # Room/furniture data
├── design/
│   └── gdd/                           # Game Design Documents
├── docs/
│   └── architecture/                  # ADRs, parity reports, performance docs
├── tests/
│   └── parity/                        # Parity test scripts and baseline
└── tools/
    ├── cache-utils/                   # Cache extraction utilities
    └── mappings/                      # Gamepack field/method mappings
```

---

## @-Referenced Documentation

- @`.claude/docs/workflow-catalog.yaml` — 8-phase OSRS pipeline with step details
- @`design/gdd/systems-index.md` — All OSRS systems and their dependencies
- @`docs/architecture/README.md` — Architecture overview and current status
- @`docs/architecture/parity-status.md` — Current parity scores
- @`.claude/docs/revision.yaml` — Pinned revision, gamepack hash
- @`tests/parity/baseline.yaml` — Latest parity baseline scores
- @`src/shared/protocol-defs.yaml` — All packet definitions

---

## Getting Started

1. **Bootstrap** (run once):
   ```
   /setup-revision-lock-and-pin
   /load-osrs-cache-full
   /import-osrsbox-complete
   ```

2. **Or run the full pipeline**:
   ```
   /full-team-orchestration-workflow 0
   ```

3. **Check current status**:
   ```bash
   bash .claude/statusline.sh
   ```

---

## Source References (open-source only)

| Resource | URL | Usage |
|----------|-----|-------|
| OpenRS2 Archive | archive.openrs2.org | Cache + XTEA acquisition |
| OSRS Wiki | oldschool.runescape.wiki | All formula/mechanic references |
| rsmod | github.com/rsmod/rsmod | Server base framework |
| osrsbox-db | github.com/osrsbox/osrsbox-db | Item/NPC/prayer data |
| RuneLite | github.com/runelite/runelite | Client base |
| PaoloKa/Interface-tool | github.com/PaoloKa/Interface-tool | Widget editing |
| RuneStar/cache-names | github.com/RuneStar/cache-names | File name mappings |
