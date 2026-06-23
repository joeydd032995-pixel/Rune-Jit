#!/usr/bin/env bash
# OSRS Emulator Studio status line — shows current phase, revision pin, parity score

REVISION="unset"
PHASE="phase-0"
PARITY="N/A"

# Read revision pin
if [ -f ".claude/docs/revision.yaml" ]; then
  REVISION=$(grep "^revision:" .claude/docs/revision.yaml 2>/dev/null | awk '{print $2}')
fi

# Detect current phase from session state
if [ -f "production/session-state/active.md" ]; then
  PHASE=$(grep "^Phase:" production/session-state/active.md 2>/dev/null | awk '{print $2}' | head -1)
fi

# Read last parity score if available
if [ -f "production/session-state/parity-score.txt" ]; then
  PARITY=$(cat production/session-state/parity-score.txt 2>/dev/null | head -1)
fi

# Detect cache status
CACHE_STATUS="no-cache"
if [ -d "cache" ] && [ "$(ls -A cache 2>/dev/null)" ]; then
  CACHE_STATUS="cache-ok"
fi

echo "OSRS-Studio | rev:${REVISION} | ${PHASE} | parity:${PARITY} | ${CACHE_STATUS}"
