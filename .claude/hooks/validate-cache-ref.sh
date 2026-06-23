#!/usr/bin/env bash
# PostToolUse(Write) hook: warns if cache/ files are referenced in newly written files

set -euo pipefail

# Get the file that was just written
WRITTEN_FILE="${TOOL_RESULT_PATH:-${1:-}}"

if [ -z "$WRITTEN_FILE" ]; then
    exit 0
fi

# Skip if the written file is in .gitignore-safe locations
if [[ "$WRITTEN_FILE" == *"cache/"* ]] || [[ "$WRITTEN_FILE" == *"data/raw/"* ]]; then
    # Writing to cache/ directly — check if it would be committed
    REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"
    if git check-ignore -q "$WRITTEN_FILE" 2>/dev/null; then
        : # file is properly gitignored, no warning needed
    else
        echo "⚠️  WARNING: File '$WRITTEN_FILE' is in cache/ but not gitignored." >&2
        echo "   Ensure cache/ is in .gitignore to prevent committing Jagex assets." >&2
    fi
    exit 0
fi

# Check if the written file references cache/ paths that suggest binary asset embedding
if [ -f "$WRITTEN_FILE" ] && grep -q 'cache/main_file_cache\|\.dat2\|\.idx[0-9]' "$WRITTEN_FILE" 2>/dev/null; then
    # Only warn if this looks like a hardcoded path, not documentation
    if grep -qE '"cache/|= "cache/' "$WRITTEN_FILE" 2>/dev/null; then
        echo "⚠️  INFO: File '$WRITTEN_FILE' references cache/ paths." >&2
        echo "   Ensure these are runtime paths, not bundled assets." >&2
    fi
fi

exit 0
