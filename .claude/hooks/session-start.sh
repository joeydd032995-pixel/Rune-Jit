#!/usr/bin/env bash
# Runs at session start: validates environment, warns about missing prerequisites

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"
LOG_DIR="$REPO_ROOT/.claude/logs"
mkdir -p "$LOG_DIR"

echo "[session-start] Rune-Jit OSRS Emulator Studio — $(date -u '+%Y-%m-%dT%H:%M:%SZ')" >> "$LOG_DIR/session.log"

# Check revision pin
REVISION_FILE="$REPO_ROOT/.claude/docs/revision.yaml"
if [ ! -f "$REVISION_FILE" ]; then
    echo "⚠️  WARNING: .claude/docs/revision.yaml not found. Run /setup-revision-lock-and-pin first." >&2
else
    REVISION=$(grep 'revision:' "$REVISION_FILE" | awk '{print $2}' | head -1)
    echo "[session-start] OSRS Revision pinned: $REVISION" >> "$LOG_DIR/session.log"
fi

# Check cache presence
CACHE_DIR="$REPO_ROOT/cache"
if [ ! -d "$CACHE_DIR" ] || [ -z "$(ls -A "$CACHE_DIR" 2>/dev/null)" ]; then
    echo "⚠️  WARNING: cache/ directory is empty. Run /load-osrs-cache-full to download the OSRS cache." >&2
fi

# Check osrsbox data
OSRSBOX_DIR="$REPO_ROOT/data/osrsbox"
if [ ! -f "$OSRSBOX_DIR/items-complete.json" ]; then
    echo "⚠️  WARNING: data/osrsbox/items-complete.json not found. Run /import-osrsbox-complete." >&2
fi

# Detect current phase from session state
STATE_FILE="$REPO_ROOT/production/session-state/phase-progress.yaml"
if [ -f "$STATE_FILE" ]; then
    CURRENT_PHASE=$(grep 'IN_PROGRESS' "$STATE_FILE" | head -1 | grep -oP 'phase_\K[0-9]+' || echo "unknown")
    echo "[session-start] Current phase: $CURRENT_PHASE" >> "$LOG_DIR/session.log"
fi

echo "[session-start] Session initialized." >> "$LOG_DIR/session.log"
exit 0
