#!/usr/bin/env bash
# PreToolUse(Bash) hook: validates git commit operations
# Blocks commits containing: hardcoded IPs, .jar files, XTEA keys, cache files

set -euo pipefail

# Only run for git commit commands
if [[ "${TOOL_INPUT:-}" != *"git commit"* ]] && [[ "${1:-}" != *"git commit"* ]]; then
    exit 0
fi

REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || echo ".")"

# Check staged files for violations
STAGED_FILES=$(git diff --cached --name-only 2>/dev/null || echo "")

if [ -z "$STAGED_FILES" ]; then
    exit 0
fi

ERRORS=0

# Check for .jar files being committed
if echo "$STAGED_FILES" | grep -q '\.jar$'; then
    echo "❌ BLOCKED: .jar files cannot be committed (gamepacks/ must be gitignored)." >&2
    echo "   Offending files: $(echo "$STAGED_FILES" | grep '\.jar$')" >&2
    ERRORS=$((ERRORS + 1))
fi

# Check for .dat2/.idx cache files
if echo "$STAGED_FILES" | grep -qE '\.(dat2|idx)$'; then
    echo "❌ BLOCKED: Cache files (.dat2, .idx) cannot be committed." >&2
    ERRORS=$((ERRORS + 1))
fi

# Check for XTEA key files
if echo "$STAGED_FILES" | grep -qE '(xtea|\.xtea|keys\.json)'; then
    echo "❌ BLOCKED: XTEA key files cannot be committed." >&2
    ERRORS=$((ERRORS + 1))
fi

# Check for hardcoded IP addresses in staged changes
STAGED_DIFF=$(git diff --cached 2>/dev/null || echo "")
if echo "$STAGED_DIFF" | grep -qE '\b([0-9]{1,3}\.){3}[0-9]{1,3}\b' ; then
    # Filter out comments and known-safe patterns
    SUSPICIOUS=$(echo "$STAGED_DIFF" | grep -E '^\+.*\b([0-9]{1,3}\.){3}[0-9]{1,3}\b' | grep -v '127\.0\.0\.1\|0\.0\.0\.0\|192\.168\.' || true)
    if [ -n "$SUSPICIOUS" ]; then
        echo "⚠️  WARNING: Possible hardcoded IP address detected in staged changes:" >&2
        echo "$SUSPICIOUS" | head -5 >&2
        # Warning only, not blocking
    fi
fi

# Check for osrsbox raw data directory
if echo "$STAGED_FILES" | grep -q '^data/osrsbox/'; then
    echo "❌ BLOCKED: data/osrsbox/ files cannot be committed (add to .gitignore)." >&2
    ERRORS=$((ERRORS + 1))
fi

if [ $ERRORS -gt 0 ]; then
    echo "" >&2
    echo "Fix the above issues before committing. Legal framing: private/educational use only." >&2
    exit 1
fi

exit 0
