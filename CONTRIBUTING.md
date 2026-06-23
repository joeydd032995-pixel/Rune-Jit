# Contributing to Rune-Jit

## Purpose & Legal Framing

Rune-Jit is a **private, educational** OSRS emulator project. It exists to study
reverse-engineering techniques, network protocol analysis, game-engine architecture,
and cache format research.

**Critical rules:**
- Do NOT commit Jagex-owned binary assets (cache `.dat2`/`.idx` files, gamepacks `.jar`)
- Do NOT redistribute XTEA decryption keys in commits
- All OSRS formulas are sourced from the public OSRS wiki (CC BY-NC-SA 3.0)
- Open-source server/client bases (rsmod, RuneLite) remain under their respective licenses

## Development Workflow

Follow the 8-phase workflow defined in `.claude/docs/workflow-catalog.yaml`:

| Phase | Description | Entry Skill |
|-------|-------------|-------------|
| 0 | Environment & Tooling | `/setup-revision-lock-and-pin` |
| 1 | Research & GDD Design | `/gdd-osrs-specialized-framework` |
| 2 | Server Core | `/implement-tick-engine-core` |
| 3 | Client Frontend | `/implement-client-rendering-pipeline` |
| 4 | Integration & Networking | `/protocol-packet-engine` |
| 5 | Content Population | `/import-osrsbox-complete` |
| 6 | Testing & Parity | `/verify-mechanic-parity-1to1` |
| 7 | Deployment | `/docker-deployment-packager` |

## Code Standards

See `.claude/rules/` for path-scoped coding standards. Key principles:
- All game values must be data-driven (no hardcoded XP/item IDs in logic code)
- Server-authoritative for all game state
- All combat/skill formulas must cite the OSRS wiki source URL
- 600ms tick budget must never be exceeded

## Commit Guidelines

- No binary files over 1MB without explicit approval
- Reference the phase and skill used: `[phase-2] implement tick engine core`
- All parity test scores must be included in PR descriptions
