#!/usr/bin/env bash
# PostToolUse(Write) for src/ files: triggers quick parity assertion

set -euo pipefail

WRITTEN_FILE="${TOOL_RESULT_PATH:-${1:-}}"

if [ -z "$WRITTEN_FILE" ]; then
    exit 0
fi

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"

# Only run for src/ changes
if [[ "$WRITTEN_FILE" != *"src/"* ]]; then
    exit 0
fi

LOG_DIR="$REPO_ROOT/.claude/logs"
mkdir -p "$LOG_DIR"

echo "[verify-parity] Source file modified: $WRITTEN_FILE" >> "$LOG_DIR/parity-checks.log"

# Only run full check if source is compiled (non-blocking advisory)
if [ ! -d "$REPO_ROOT/build/classes" ]; then
    echo "ℹ️  Parity check skipped (project not yet compiled). Run ./gradlew build first." >&2
    exit 0
fi

# Detect which system was modified and run targeted parity check
if [[ "$WRITTEN_FILE" == *"combat"* ]]; then
    SYSTEM="combat"
elif [[ "$WRITTEN_FILE" == *"woodcutting"* ]]; then
    SYSTEM="woodcutting"
elif [[ "$WRITTEN_FILE" == *"mining"* ]]; then
    SYSTEM="mining"
elif [[ "$WRITTEN_FILE" == *"fishing"* ]]; then
    SYSTEM="fishing"
elif [[ "$WRITTEN_FILE" == *"prayer"* ]]; then
    SYSTEM="prayer"
else
    SYSTEM=""
fi

if [ -n "$SYSTEM" ]; then
    PARITY_FILE="$REPO_ROOT/tests/parity/${SYSTEM}/"
    if [ -d "$PARITY_FILE" ]; then
        echo "ℹ️  Run parity check: ./gradlew test --tests \"*${SYSTEM^}Parity*\"" >&2
    fi
fi

exit 0
