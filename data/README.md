# Data Directory

This directory contains game data for the OSRS emulator. It is **not** populated
by default — run the import skills after Phase 0 environment setup.

## Contents

```
data/
├── osrsbox/          # Item, NPC, object, prayer definitions (from osrsbox-db)
│   ├── items-complete.json
│   ├── monsters-complete.json
│   ├── prayers.json
│   └── items-icons/
├── schemas/          # JSON Schema definitions for validation
└── README.md         # This file
```

## Population Instructions

### 1. Import osrsbox-db data (items, NPCs, objects, prayers)
```
/import-osrsbox-complete
```
This skill fetches from https://github.com/osrsbox/osrsbox-db and populates
`data/osrsbox/` with validated JSON for all 23,000+ items, 5,000+ monsters,
and all prayers.

### 2. Download cache (models, sprites, maps, sounds)
```
/load-osrs-cache-full
```
Downloads the target OSRS revision cache from https://archive.openrs2.org.
Cache files go into `cache/` (gitignored). See `cache/CACHE-REPORT.md` after
completion for coverage statistics.

## Data Licensing

- osrsbox-db data: CC BY 4.0 (https://github.com/osrsbox/osrsbox-db/blob/master/LICENSE)
- OSRS wiki formulas: CC BY-NC-SA 3.0
- No Jagex-proprietary data is committed to this repository
