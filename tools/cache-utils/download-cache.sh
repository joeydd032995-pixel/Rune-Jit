#!/usr/bin/env bash
#
# download-cache.sh — Fetch the pinned OSRS cache + XTEA keys from the OpenRS2 Archive.
#
# This is REAL tooling for the /load-osrs-cache-full skill. It reads the pinned
# revision from .claude/docs/revision.yaml and downloads the corresponding cache
# disk store and map XTEA keys into cache/ (which is .gitignored — never committed).
#
# Legal: private/educational use only. No Jagex assets are redistributed by this
# repo; this script fetches them at runtime from the community OpenRS2 archive.
#
# Usage:
#   bash tools/cache-utils/download-cache.sh
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
REVISION_YAML="$REPO_ROOT/.claude/docs/revision.yaml"
CACHE_DIR="$REPO_ROOT/cache"
XTEA_DIR="$CACHE_DIR/xtea"

err() { echo "ERROR: $*" >&2; exit 1; }

command -v curl >/dev/null || err "curl is required"
[ -f "$REVISION_YAML" ] || err "No revision pin found. Run /setup-revision-lock-and-pin first."

# --- Parse the pin (minimal YAML scraping; no external yaml dep required) -----
get() { grep -E "^[[:space:]]*$1:" "$REVISION_YAML" | head -1 | sed -E "s/^[^:]*:[[:space:]]*//; s/[[:space:]]*(#.*)?$//; s/^\"//; s/\"$//"; }

CACHE_ID="$(get cache_id)"
DOWNLOAD_URL="$(get download_url)"
KEYS_URL="$(get keys_url)"
REVISION="$(get revision)"

[ -n "$CACHE_ID" ] || err "Could not read cache_id from $REVISION_YAML"

echo "Rune-Jit cache fetch"
echo "  revision : ${REVISION:-?}"
echo "  cache id : $CACHE_ID"
echo "  target   : $CACHE_DIR"
echo

mkdir -p "$CACHE_DIR" "$XTEA_DIR"

# --- Disk store (the cache itself, ~163 MiB) ---------------------------------
DISK_ZIP="$CACHE_DIR/disk-${CACHE_ID}.zip"
echo ">> Downloading cache disk store..."
curl -fSL --retry 4 --retry-delay 2 -o "$DISK_ZIP" "$DOWNLOAD_URL" \
  || err "Cache download failed. Try a fallback cache id from revision.yaml notes."
echo ">> Unpacking..."
( cd "$CACHE_DIR" && rm -rf cache && unzip -oq "$DISK_ZIP" )
echo "   cache unpacked to $CACHE_DIR/cache/"

# --- Map XTEA keys ------------------------------------------------------------
echo ">> Downloading XTEA keys..."
curl -fSL --retry 4 --retry-delay 2 -o "$XTEA_DIR/keys.json" "$KEYS_URL" \
  || err "XTEA key download failed."
KEY_COUNT="$(grep -o '"mapsquare"' "$XTEA_DIR/keys.json" | wc -l | tr -d ' ')"
echo "   XTEA keys saved ($KEY_COUNT mapsquares)"

echo
echo "Done. Cache + keys are in $CACHE_DIR (gitignored)."
echo "Next: run /load-osrs-cache-full to validate coverage and generate CACHE-REPORT.md,"
echo "      then /import-osrsbox-complete for item/NPC data."
