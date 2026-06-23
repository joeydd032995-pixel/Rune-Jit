#!/usr/bin/env bash
# SubagentStart hook: logs agent name + timestamp to agent-activity.log

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"
LOG_DIR="$REPO_ROOT/.claude/logs"
mkdir -p "$LOG_DIR"

AGENT_NAME="${SUBAGENT_NAME:-${AGENT_NAME:-unknown}}"
TIMESTAMP=$(date -u '+%Y-%m-%dT%H:%M:%SZ')

echo "[$TIMESTAMP] START agent=$AGENT_NAME" >> "$LOG_DIR/agent-activity.log"

# Track concurrent agent count
RUNNING=$(grep -c 'START' "$LOG_DIR/agent-activity.log" 2>/dev/null || echo "0")
STOPPED=$(grep -c 'STOP ' "$LOG_DIR/agent-activity.log" 2>/dev/null || echo "0")
CONCURRENT=$((RUNNING - STOPPED))

if [ "$CONCURRENT" -gt 10 ]; then
    echo "⚠️  INFO: $CONCURRENT agents running concurrently." >&2
fi

exit 0
