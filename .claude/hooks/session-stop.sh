#!/usr/bin/env bash
# Runs at session end: saves session state and logs active story

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"
LOG_DIR="$REPO_ROOT/.claude/logs"
STATE_DIR="$REPO_ROOT/production/session-state"
mkdir -p "$LOG_DIR" "$STATE_DIR"

TIMESTAMP=$(date -u '+%Y-%m-%dT%H:%M:%SZ')

echo "[session-stop] Session ended at $TIMESTAMP" >> "$LOG_DIR/session.log"

# Record parity score if available
PARITY_FILE="$STATE_DIR/parity-score.yaml"
if [ -f "$PARITY_FILE" ]; then
    SCORE=$(grep 'score:' "$PARITY_FILE" | awk '{print $2}' | head -1)
    echo "[session-stop] Final parity score: $SCORE" >> "$LOG_DIR/session.log"
fi

# Write session summary
SUMMARY="$LOG_DIR/session-summary-$(date -u '+%Y%m%d-%H%M%S').md"
cat > "$SUMMARY" << EOF
# Session Summary — $TIMESTAMP

## Session End

Session concluded. Check git log for changes made.

To resume: start a new session in Rune-Jit directory.
EOF

echo "[session-stop] Summary written to $SUMMARY" >> "$LOG_DIR/session.log"
exit 0
