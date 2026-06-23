---
name: economy-and-ge-simulator
description: "Implements the Grand Exchange, shop pricing formulas, restock mechanics, and special currency shops (tokkul, slayer points). Includes economy simulation for inflation testing."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /economy-and-ge-simulator

Implements the OSRS Grand Exchange and shop economy systems.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| osrsbox imported (items with alch values) | Yes |
| `data/shops/shops.json` exists | Recommended |
| Inventory/bank system working | Yes |

## Phase 2: Shop System Implementation

Spawn `shop-system-engineer` to implement:
- General stores (accept player-sold items)
- Specialty shops (fixed stock, higher sell prices for specialty items)
- Ironman shops (per-player stock)
- Special currency shops (tokkul, slayer points, etc.)

Price formulas from wiki:
```
Buy (stock < normal): base * (base / (stock + 1)) * 0.03
Buy (stock >= normal): base_price
Sell (general store): base * 0.30
Sell (specialty): base * 1.00 (for specialty items)
```

Restock: 1 item per `restockRate` ticks (default 100 = 60 seconds).

## Phase 3: Grand Exchange Implementation

Spawn `economy-balancer` to implement `src/server/economy/GrandExchange.kt`:
- Buy offers: player posts max price
- Sell offers: player posts min price
- Matching: buy price ≥ sell price → match at sell price
- Partial fills: partially fulfilled orders persist
- Price guide: display current buy/sell prices

## Phase 4: Alch Price Validation

Verify alchemy values from osrsbox match wiki:
```kotlin
// High alch = item.highalch (from osrsbox)
// Low alch = item.lowalch
// Alch profit = highalch - nature_rune_cost (200gp)
```

## Phase 5: Special Shops

Spawn `shop-system-engineer` for special shops:
- TzHaar (tokkul currency): Fire cape rewards
- Slayer reward shop: slayer points
- Pest Control: void equipment (PC points)
- Nightmare Zone: imbue scrolls (NMZ points)
- LMS: Last Man Standing tokens

## Phase 6: Economy Tests

Spawn `economy-tester` to run:
- GE price matching tests
- Shop restock rate tests
- Alch value tests (50 items against wiki)
- Overstocked item drain tests
- Ironman shop isolation tests

## Phase 7: Inflation Simulation

Run 7-day economy simulation:
- 100 simulated bots buying/selling
- Check key item prices stay within ±20% of initial
- Report inflation index

## Error Recovery

| Error | Recovery |
|-------|---------|
| Missing shop data | Create stub shop with 0 items |
| GE matching not firing | Check offer tick processing in scheduler |
| Wrong alch values | Re-fetch from osrsbox; check item ID mapping |

## Nuances

- GE processes every 2 minutes (200 ticks), not every tick
- Free-to-play items have different shop sell limits
- Iron Man mode: can't buy from GE; can sell to general stores
- Ultimate Ironman: no bank, no GE
- Shop stock for ironmen is per-player (they fill their own stock by killing monsters)

## Next Steps

1. Run `/verify-mechanic-parity-1to1 economy` for parity score
2. Run `/npc-drop-table-importer` (NPC drop tables feed economy)
