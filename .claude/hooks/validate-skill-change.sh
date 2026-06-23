#!/usr/bin/env bash
# PostToolUse(Edit) hook: runs parity smoke test when skill source files are modified

set -euo pipefail

EDITED_FILE="${TOOL_RESULT_PATH:-${1:-}}"

if [ -z "$EDITED_FILE" ]; then
    exit 0
fi

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"

# Only run for skill source files
if [[ "$EDITED_FILE" != *"src/server/skills/"* ]]; then
    exit 0
fi

# Identify which skill was modified
SKILL_NAME=$(echo "$EDITED_FILE" | grep -oP 'src/server/skills/\K[^/]+' || echo "unknown")

echo "🔍 Skill '$SKILL_NAME' was modified. Running parity smoke test..." >&2

# Run quick parity smoke test if test file exists
PARITY_TEST="$REPO_ROOT/tests/parity/skilling/${SKILL_NAME}-xp.test.kt"
if [ -f "$PARITY_TEST" ]; then
    # Try to run the specific test (non-blocking — just advisory)
    if command -v ./gradlew &>/dev/null; then
        cd "$REPO_ROOT"
        ./gradlew test --tests "*${SKILL_NAME^}*" --continue 2>&1 | tail -5 || true
    fi
else
    echo "ℹ️  No parity test found for '$SKILL_NAME' at $PARITY_TEST" >&2
    echo "   Consider adding tests/parity/skilling/${SKILL_NAME}-xp.test.kt" >&2
fi

# Check if XP values are hardcoded (violation of server-skills.md rule)
if grep -qE '\b(25\.0|50\.0|87\.5|100\.0|175\.0|200\.0)\b' "$EDITED_FILE" 2>/dev/null; then
    echo "⚠️  WARNING: Possible hardcoded XP values in '$EDITED_FILE'." >&2
    echo "   XP values should be data-driven from data/osrsbox/ or skill data files." >&2
fi

exit 0
