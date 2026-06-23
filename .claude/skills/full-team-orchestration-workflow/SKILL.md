---
name: full-team-orchestration-workflow
description: "Conducts the entire OSRS emulator development pipeline from Phase 0 to Phase 7, spawning all specialist agents in dependency order. Use this to start development from scratch or resume from a named phase."
argument-hint: "[start_phase: 0|1|2|3|4|5|6|7]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: opus
---

# /full-team-orchestration-workflow [start_phase]

Orchestrates all 84 agents across 8 phases for complete OSRS emulator development.

## Phase 1: State Assessment

Read `.claude/docs/workflow-catalog.yaml` and `.claude/docs/revision.yaml`. Determine current phase from:
1. `[start_phase]` argument (override)
2. `.claude/logs/phase-state.yaml` (resume from last session)
3. Default: Phase 0 if no state file exists

Spawn `orchestration-conductor` to:
- Map all completed artifacts
- Identify blockers from previous sessions
- Produce phase dependency graph
- Confirm start phase with user via `AskUserQuestion` if ambiguous

## Phase 2: Phase 0 — Environment & Tooling

**Prerequisite for all other phases.**

Spawn in parallel:
- `dependency-validator` → check Java 17, Gradle 8, Python 3.10+, jq, curl
- `legal-compliance-checker` → review all referenced resources for license compliance

Then sequentially:
1. Run `/setup-revision-lock-and-pin` → pins target revision in `revision.yaml`
2. Run `/load-osrs-cache-full` → downloads cache + XTEA keys
3. Run `/import-osrsbox-complete` → populates `data/osrsbox/`

**Phase 0 Exit Criteria:**
- [ ] `cache/CACHE-REPORT.md` exists with ≥85% XTEA coverage
- [ ] `data/osrsbox/items/` contains ≥23,000 item files
- [ ] `.claude/docs/revision.yaml` pinned and signed

Skip to Phase 1 if all exit criteria met.

## Phase 3: Phase 1 — Research, GDD & System Design

Spawn `osrs-systems-mapper` to build `design/gdd/systems-index.md`.

Spawn GDD authors in parallel batches:

**Batch 1 (core systems):**
- Run `/gdd-osrs-specialized-framework combat`
- Run `/gdd-osrs-specialized-framework woodcutting`
- Run `/gdd-osrs-specialized-framework mining`

**Batch 2 (skills):**
- Run `/gdd-osrs-specialized-framework fishing`
- Run `/gdd-osrs-specialized-framework agility`
- Run `/gdd-osrs-specialized-framework slayer`
- Run `/gdd-osrs-specialized-framework herblore`
- Run `/gdd-osrs-specialized-framework prayer`

**Batch 3 (content):**
- Spawn `quest-state-designer` for 5 quest state machines (Tutorial Island + 4 F2P quests)
- Spawn `economy-balancer` for GE and shop system design
- Spawn `performance-planner` for performance budgets

**Phase 1 Exit Criteria:**
- [ ] All core system GDDs created in `design/gdd/`
- [ ] `design/gdd/systems-index.md` populated
- [ ] `docs/architecture/performance-budget.md` created

## Phase 4: Phase 2 — Server Core

**Sequential foundation layer:**
1. Run `/implement-tick-engine-core` — must complete before all other server work
2. Run `/protocol-packet-engine` — must complete before login flow

**Parallel skill implementation (after tick engine):**
- Run `/implement-skill-action-framework woodcutting`
- Run `/implement-skill-action-framework mining`
- Run `/implement-skill-action-framework fishing`
- Run `/implement-skill-action-framework agility`
- Run `/implement-skill-action-framework cooking`
- Run `/implement-skill-action-framework firemaking`

**Sequential combat chain:**
1. Run `/combat-engine-full` — depends on tick engine + osrsbox data
2. Run `/prayer-effect-simulator` — depends on combat engine

**Parallel infrastructure:**
- Run `/pathfinding-and-clipping-engine` — depends on tick engine, parallel with combat
- Run `/quest-and-diary-state-machine` — depends on tick engine

**Phase 2 Exit Criteria:**
- [ ] Server starts and accepts connections
- [ ] Player can log in, move, and use at least 3 skills
- [ ] Combat damage rolls match wiki formulas (≥85%)
- [ ] `/automated-parity-testing-suite skills` score ≥80%

## Phase 5: Phase 3 — Client Frontend

**Sequential chain:**
1. Run `/implement-client-rendering-pipeline` — base renderer required first
2. Run `/paoloka-interface-integrator` — depends on renderer
3. Run `/protocol-packet-engine` (client-side completion)

**Phase 3 Exit Criteria:**
- [ ] Client connects to server and renders game world
- [ ] Chat interface functional
- [ ] Inventory/bank interface functional
- [ ] Combat interface (HP/Prayer/Run orbs) functional

## Phase 6: Phase 4 — Integration & Networking

Spawn `protocol-sync-validator` for end-to-end packet coverage audit.
Spawn `desync-investigator` for server/client state divergence analysis.
Spawn `integration-tester` for login → game → logout flow test.

**Phase 4 Exit Criteria:**
- [ ] No known desync conditions
- [ ] Packet coverage ≥95%
- [ ] 5-player simultaneous login test passes

## Phase 7: Phase 5 — Content Population & Polish

**Parallel content work:**
- Run `/economy-and-ge-simulator`
- Run `/raids-and-instanced-content-generator cox` → `tob` → `toa`
- Run `/npc-drop-table-importer`
- Run `/construction-room-builder`
- Spawn `content-parity-verifier` for item/NPC completeness

**Phase 5 Exit Criteria:**
- [ ] Grand Exchange functional
- [ ] At least 1 raid fully implemented
- [ ] NPC drop tables imported for 500+ monsters
- [ ] Tutorial Island completable

## Phase 8: Phase 6 — Comprehensive Testing & Iteration

Run `/automated-parity-testing-suite all` — global score must be ≥90%.
Run `/performance-profiling-and-optimization 500` — P99 tick <580ms at 500 players.
Spawn `bug-triager` for triage of all open issues.

Iterate: fix regressions → re-run parity → fix performance → re-run load test.

**Phase 6 Exit Criteria:**
- [ ] Global parity score ≥90%
- [ ] P99 tick <580ms at 500 concurrent players
- [ ] Memory <4GB at 500 players
- [ ] Zero CRITICAL bugs open

## Phase 9: Phase 7 — Deployment, Maintenance & Revision Handling

Run `/docker-deployment-packager` → produces `dist/rune-jit-server.tar.gz`
Run `/documentation-auto-generator` → produces complete docs set
Spawn `release-packager` for versioned release bundle.

Set up revision watch:
- Configure `revision-update-adapter` to run when new OSRS revision detected
- Schedule weekly parity regression runs

**Phase 7 Exit Criteria:**
- [ ] Docker deployment working
- [ ] Release bundle created
- [ ] `docs/architecture/` fully populated
- [ ] Revision update process documented

## Error Recovery

| Error | Recovery |
|-------|---------|
| Phase agent fails | Re-spawn with additional context; check logs in `.claude/logs/agent-activity.log` |
| Exit criteria not met | Do not advance to next phase; spawn `bug-triager` |
| Performance budget missed | Spawn `performance-profiler` before advancing to Phase 7 |
| Parity score stalls below target | Spawn `mechanic-parity-analyst` to identify highest-impact gaps |

## Nuances

- Phases are not strictly linear — Phase 3 (client) can proceed in parallel with Phase 2 back-end work
- Each phase exit criteria is a hard gate; do not skip even under time pressure
- Spawn `orchestration-conductor` at the start of each day to re-assess blockers
- `.claude/logs/phase-state.yaml` must be updated at every phase transition
- The GDD is a living document — update it throughout implementation, not just at Phase 1

## Next Steps

After Phase 7 completion:
1. Announce private beta to a small group of trusted testers
2. Collect bug reports; run weekly parity regressions
3. Monitor for new OSRS revisions with `/revision-update-adapter`
