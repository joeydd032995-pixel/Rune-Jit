#!/usr/bin/env bash
# SubagentStop hook: records agent completion and elapsed time

set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"
LOG_DIR="$REPO_ROOT/.claude/logs"
mkdir -p "$LOG_DIR"

AGENT_NAME="${SUBAGENT_NAME:-${AGENT_NAME:-unknown}}"
TIMESTAMP=$(date -u '+%Y-%m-%dT%H:%M:%SZ')
STATUS="${SUBAGENT_STATUS:-completed}"

echo "[$TIMESTAMP] STOP  agent=$AGENT_NAME status=$STATUS" >> "$LOG_DIR/agent-activity.log"

# Compute elapsed time if start time is available
START_TIME=$(grep "START agent=$AGENT_NAME" "$LOG_DIR/agent-activity.log" | tail -1 | grep -oP '\[\K[^\]]+' || echo "")
if [ -n "$START_TIME" ]; then
    START_EPOCH=$(date -d "$START_TIME" '+%s' 2>/dev/null || echo "0")
    END_EPOCH=$(date '+%s')
    ELAPSED=$((END_EPOCH - START_EPOCH))
    echo "[$TIMESTAMP] TIME  agent=$AGENT_NAME elapsed=${ELAPSED}s" >> "$LOG_DIR/agent-activity.log"
fi

exit 0
