#!/usr/bin/env python3
"""
import-osrsbox.py — Fetch and validate osrsbox-db data for Rune-Jit.

Downloads items-complete.json, monsters-complete.json, and prayers-complete.json
from the osrsbox-db GitHub repository into data/osrsbox/. Validates record counts
and required fields, then generates Kotlin ID constant files in src/server/data/.

Legal: osrsbox-db is CC BY 4.0 (https://github.com/osrsbox/osrsbox-db/blob/master/LICENSE).
No Jagex intellectual property is downloaded or committed by this script.

Usage:
    python3 tools/cache-utils/import-osrsbox.py
    python3 tools/cache-utils/import-osrsbox.py --skip-kotlin
    python3 tools/cache-utils/import-osrsbox.py --output-dir /path/to/data/osrsbox
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from datetime import date
from pathlib import Path

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
DATA_DIR = REPO_ROOT / "data" / "osrsbox"
KOTLIN_DIR = REPO_ROOT / "src" / "server" / "data"
STATUS_FILE = REPO_ROOT / "data" / "osrsbox" / ".import-status.json"

# ---------------------------------------------------------------------------
# Dataset definitions
# ---------------------------------------------------------------------------

OSRSBOX_RAW = "https://raw.githubusercontent.com/osrsbox/osrsbox-db/master/docs"

DATASETS: list[dict] = [
    {
        "name": "items",
        "filename": "items-complete.json",
        "url": f"{OSRSBOX_RAW}/items-complete.json",
        "min_count": 20_000,
        "required_fields": ["id", "name", "tradeable_on_ge"],
        "description": "Items (weapons, armour, resources, consumables)",
    },
    {
        "name": "monsters",
        "filename": "monsters-complete.json",
        "url": f"{OSRSBOX_RAW}/monsters-complete.json",
        "min_count": 4_000,
        "required_fields": ["id", "name", "hitpoints"],
        "description": "Monsters and NPCs",
    },
    {
        "name": "prayers",
        "filename": "prayers-complete.json",
        "url": f"{OSRSBOX_RAW}/prayers-complete.json",
        "fallback_url": f"{OSRSBOX_RAW}/prayers.json",
        "min_count": 25,
        "required_fields": ["id", "name"],
        "description": "Prayer definitions (drain rate, bonuses)",
    },
]

# Kotlin constant files to generate after import
KOTLIN_SPECS: list[dict] = [
    {
        "dataset": "items",
        "output": "ItemIds.kt",
        "object_name": "ItemIds",
        "id_field": "id",
        "name_fields": ["wiki_name", "name"],
        "package": "net.runelite.server.data",
        "header_comment": (
            "Item ID constants generated from osrsbox-db.\n"
            " * Source: https://github.com/osrsbox/osrsbox-db (CC BY 4.0)\n"
            " * Regenerate: python3 tools/cache-utils/import-osrsbox.py"
        ),
        "filter_fn": None,
    },
    {
        "dataset": "monsters",
        "output": "NpcIds.kt",
        "object_name": "NpcIds",
        "id_field": "id",
        "name_fields": ["name"],
        "package": "net.runelite.server.data",
        "header_comment": (
            "NPC ID constants generated from osrsbox-db.\n"
            " * Source: https://github.com/osrsbox/osrsbox-db (CC BY 4.0)\n"
            " * Regenerate: python3 tools/cache-utils/import-osrsbox.py"
        ),
        "filter_fn": None,
    },
]

# ---------------------------------------------------------------------------
# Utilities
# ---------------------------------------------------------------------------

BOLD = "\033[1m"
GREEN = "\033[32m"
YELLOW = "\033[33m"
RED = "\033[31m"
CYAN = "\033[36m"
RESET = "\033[0m"


def ok(msg: str) -> None:
    print(f"  {GREEN}✓{RESET} {msg}")


def warn(msg: str) -> None:
    print(f"  {YELLOW}⚠{RESET} {msg}", file=sys.stderr)


def err(msg: str) -> None:
    print(f"  {RED}✗{RESET} {msg}", file=sys.stderr)


def step(msg: str) -> None:
    print(f"\n{BOLD}{CYAN}>>{RESET} {msg}")


# ---------------------------------------------------------------------------
# Download with retry
# ---------------------------------------------------------------------------

def download(url: str, dest: Path, retries: int = 3, backoff: float = 5.0) -> bool:
    """Download *url* to *dest*, retrying on transient errors. Returns True on success."""
    dest.parent.mkdir(parents=True, exist_ok=True)
    tmp = dest.with_suffix(dest.suffix + ".tmp")

    for attempt in range(1, retries + 1):
        try:
            print(f"    Fetching {url}", end="", flush=True)
            req = urllib.request.Request(url, headers={"User-Agent": "Rune-Jit/1.0 (educational OSRS emulator)"})
            with urllib.request.urlopen(req, timeout=60) as resp:
                data = resp.read()
            tmp.write_bytes(data)
            tmp.replace(dest)
            size_kb = dest.stat().st_size // 1024
            print(f"  ({size_kb} KB)")
            return True
        except urllib.error.HTTPError as e:
            print(f"  → HTTP {e.code}")
            if e.code in (404, 403):
                return False
            if attempt < retries:
                time.sleep(backoff * attempt)
        except Exception as e:
            print(f"  → {e}")
            if attempt < retries:
                time.sleep(backoff * attempt)

    if tmp.exists():
        tmp.unlink()
    return False


# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

def load_json(path: Path) -> dict | list | None:
    try:
        with path.open(encoding="utf-8") as f:
            return json.load(f)
    except json.JSONDecodeError as e:
        err(f"Invalid JSON in {path.name}: {e}")
        return None


def validate_dataset(data: dict | list, spec: dict) -> tuple[int, list[str]]:
    """
    Validate dataset against spec. Returns (record_count, list_of_warnings).
    osrsbox JSON files are dicts keyed by record ID, not arrays.
    """
    warnings: list[str] = []

    if isinstance(data, dict):
        records = list(data.values())
    elif isinstance(data, list):
        records = data
    else:
        return 0, ["Unexpected JSON root type"]

    count = len(records)

    if count < spec["min_count"]:
        warnings.append(
            f"Only {count} records — expected ≥ {spec['min_count']}. Dataset may be incomplete."
        )

    missing_fields: dict[str, int] = {}
    null_fields: dict[str, int] = {}

    for rec in records[:500]:  # spot-check first 500
        if not isinstance(rec, dict):
            continue
        for field in spec["required_fields"]:
            if field not in rec:
                missing_fields[field] = missing_fields.get(field, 0) + 1
            elif rec[field] is None:
                null_fields[field] = null_fields.get(field, 0) + 1

    for field, n in missing_fields.items():
        warnings.append(f"Required field '{field}' missing in {n} of first 500 records")

    return count, warnings


# ---------------------------------------------------------------------------
# Kotlin constant generation
# ---------------------------------------------------------------------------

_INVALID_KT_CHAR = re.compile(r"[^A-Z0-9_]")
_LEADING_DIGIT = re.compile(r"^[0-9]")


def to_const_name(raw: str) -> str:
    """Convert an item/NPC name to a valid Kotlin constant name."""
    # Normalise apostrophes, hyphens, parentheses, spaces → underscore
    name = raw.upper()
    name = name.replace("'", "").replace("(", "").replace(")", "")
    name = _INVALID_KT_CHAR.sub("_", name)
    # Collapse repeated underscores
    name = re.sub(r"_+", "_", name).strip("_")
    # Can't start with a digit
    if _LEADING_DIGIT.match(name):
        name = "ID_" + name
    return name or "UNNAMED"


def generate_kotlin_constants(
    data: dict | list,
    spec: dict,
    output_dir: Path,
) -> tuple[int, Path]:
    """Generate a Kotlin object file of ID constants. Returns (constant_count, file_path)."""
    if isinstance(data, dict):
        records = list(data.values())
    else:
        records = list(data)

    # Build name → id mapping, deduplicating constant names
    seen_names: dict[str, int] = {}
    duplicates: dict[str, int] = {}
    constants: list[tuple[str, int, str]] = []  # (const_name, id_value, raw_name)

    for rec in records:
        if not isinstance(rec, dict):
            continue
        item_id = rec.get(spec["id_field"])
        if item_id is None:
            continue

        raw_name: str | None = None
        for nf in spec["name_fields"]:
            candidate = rec.get(nf)
            if candidate and isinstance(candidate, str) and candidate.strip():
                raw_name = candidate.strip()
                break

        if not raw_name:
            continue

        const = to_const_name(raw_name)

        if const in seen_names:
            duplicates[const] = duplicates.get(const, 1) + 1
            const = f"{const}_{item_id}"

        seen_names[const] = item_id
        constants.append((const, int(item_id), raw_name))

    constants.sort(key=lambda t: t[1])

    package = spec["package"]
    obj_name = spec["object_name"]
    header = spec["header_comment"]

    lines = [
        f"// {header.splitlines()[0]}",
        *[f" * {line}" for line in header.splitlines()[1:]],
        f"@file:Suppress(\"MemberVisibilityCanBePrivate\", \"unused\")",
        "",
        f"package {package}",
        "",
        f"object {obj_name} {{",
    ]
    for const, id_val, raw in constants:
        lines.append(f"    const val {const} = {id_val}  // {raw}")
    lines.append("}")
    lines.append("")

    output_dir.mkdir(parents=True, exist_ok=True)
    out_path = output_dir / spec["output"]
    out_path.write_text("\n".join(lines), encoding="utf-8")
    return len(constants), out_path


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(
        description="Import osrsbox-db datasets into Rune-Jit data layer",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    p.add_argument(
        "--output-dir",
        type=Path,
        default=DATA_DIR,
        help=f"Destination for osrsbox JSON files (default: {DATA_DIR})",
    )
    p.add_argument(
        "--kotlin-dir",
        type=Path,
        default=KOTLIN_DIR,
        help=f"Destination for generated Kotlin constants (default: {KOTLIN_DIR})",
    )
    p.add_argument(
        "--skip-kotlin",
        action="store_true",
        help="Skip Kotlin constant file generation",
    )
    p.add_argument(
        "--skip-download",
        action="store_true",
        help="Skip download if files already exist in output-dir",
    )
    p.add_argument(
        "--retries",
        type=int,
        default=3,
        help="Number of download retries per file (default: 3)",
    )
    return p.parse_args()


def main() -> int:
    args = parse_args()
    output_dir: Path = args.output_dir
    kotlin_dir: Path = args.kotlin_dir

    print(f"\n{BOLD}Rune-Jit — osrsbox-db Import{RESET}")
    print(f"  output  : {output_dir}")
    print(f"  kotlin  : {'(skipped)' if args.skip_kotlin else kotlin_dir}")
    print(f"  source  : https://github.com/osrsbox/osrsbox-db (CC BY 4.0)")

    output_dir.mkdir(parents=True, exist_ok=True)

    loaded: dict[str, dict | list] = {}
    record_counts: dict[str, int] = {}
    overall_ok = True

    # ------------------------------------------------------------------
    # Phase 1: Download
    # ------------------------------------------------------------------
    step("Phase 1: Downloading datasets")

    for ds in DATASETS:
        dest = output_dir / ds["filename"]

        if args.skip_download and dest.exists():
            print(f"    {ds['description']}: already present, skipping download")
        else:
            success = download(ds["url"], dest, retries=args.retries)
            if not success:
                fallback = ds.get("fallback_url")
                if fallback:
                    warn(f"Primary URL failed for {ds['filename']}, trying fallback...")
                    alt_filename = fallback.split("/")[-1]
                    alt_dest = output_dir / alt_filename
                    success = download(fallback, alt_dest, retries=args.retries)
                    if success and alt_dest != dest:
                        # Rename fallback to expected filename
                        alt_dest.replace(dest)
                if not success:
                    err(f"Download failed for {ds['description']}. Check network access to GitHub.")
                    overall_ok = False
                    continue

        if dest.exists():
            ok(f"{ds['description']}: downloaded to {dest.name}")
        else:
            err(f"File not found after download: {dest}")
            overall_ok = False

    # ------------------------------------------------------------------
    # Phase 2: Load + Validate
    # ------------------------------------------------------------------
    step("Phase 2: Validating datasets")

    for ds in DATASETS:
        dest = output_dir / ds["filename"]
        if not dest.exists():
            warn(f"Skipping validation for {ds['filename']} (not downloaded)")
            continue

        data = load_json(dest)
        if data is None:
            overall_ok = False
            continue

        count, warnings = validate_dataset(data, ds)
        record_counts[ds["name"]] = count

        if warnings:
            for w in warnings:
                warn(f"{ds['description']}: {w}")
            if count < ds["min_count"]:
                overall_ok = False
        else:
            ok(f"{ds['description']}: {count:,} records — validation passed")

        loaded[ds["name"]] = data

    # ------------------------------------------------------------------
    # Phase 3: Key item spot-checks
    # ------------------------------------------------------------------
    step("Phase 3: Spot-checking known items and monsters")

    ITEM_CHECKS = [
        (4151, "Abyssal whip", {"highalch": 72_000}),
        (1513, "Magic logs", {"tradeable_on_ge": True}),
        (537, "Dragon bones", {}),
        (995, "Coins", {}),
    ]
    NPC_CHECKS = [
        (2, "Man", {}),
        (50, "Cow", {"hitpoints": 8}),
    ]

    items_data = loaded.get("items", {})
    monsters_data = loaded.get("monsters", {})

    def find_by_id(data: dict | list, item_id: int) -> dict | None:
        if isinstance(data, dict):
            return data.get(str(item_id)) or data.get(item_id)
        for rec in data:
            if isinstance(rec, dict) and rec.get("id") == item_id:
                return rec
        return None

    item_spot_ok = True
    for item_id, expected_name, expected_fields in ITEM_CHECKS:
        rec = find_by_id(items_data, item_id)
        if rec is None:
            warn(f"Item {item_id} ({expected_name}) not found in dataset")
            item_spot_ok = False
            continue
        actual_name = rec.get("wiki_name") or rec.get("name", "")
        if expected_name.lower() not in actual_name.lower():
            warn(f"Item {item_id}: name '{actual_name}' does not match expected '{expected_name}'")
        for field, val in expected_fields.items():
            if rec.get(field) != val:
                warn(f"Item {item_id} ({expected_name}): {field}={rec.get(field)!r}, expected {val!r}")

    if item_spot_ok and items_data:
        ok("Item spot-checks passed (abyssal whip, magic logs, dragon bones, coins)")

    npc_spot_ok = True
    for npc_id, expected_name, expected_fields in NPC_CHECKS:
        rec = find_by_id(monsters_data, npc_id)
        if rec is None:
            warn(f"NPC {npc_id} ({expected_name}) not found in dataset")
            npc_spot_ok = False
            continue
        for field, val in expected_fields.items():
            if rec.get(field) != val:
                warn(f"NPC {npc_id} ({expected_name}): {field}={rec.get(field)!r}, expected {val!r}")

    if npc_spot_ok and monsters_data:
        ok("Monster spot-checks passed (Man, Cow)")

    # ------------------------------------------------------------------
    # Phase 4: Generate Kotlin constants
    # ------------------------------------------------------------------
    if not args.skip_kotlin:
        step("Phase 4: Generating Kotlin ID constants")
        for kt_spec in KOTLIN_SPECS:
            dataset_name = kt_spec["dataset"]
            data = loaded.get(dataset_name)
            if data is None:
                warn(f"Skipping {kt_spec['output']} — {dataset_name} dataset not loaded")
                continue
            count, out_path = generate_kotlin_constants(data, kt_spec, kotlin_dir)
            ok(f"{kt_spec['output']}: {count:,} constants → {out_path.relative_to(REPO_ROOT)}")
    else:
        step("Phase 4: Kotlin generation skipped (--skip-kotlin)")

    # ------------------------------------------------------------------
    # Phase 5: Write import status
    # ------------------------------------------------------------------
    step("Phase 5: Writing import status")

    status = {
        "import_date": date.today().isoformat(),
        "source": "https://github.com/osrsbox/osrsbox-db",
        "license": "CC BY 4.0",
        "datasets": {
            name: {"count": count, "file": (output_dir / f"{name}s-complete.json").name}
            for name, count in record_counts.items()
        },
        "kotlin_generated": not args.skip_kotlin,
        "overall_ok": overall_ok,
    }

    STATUS_FILE.parent.mkdir(parents=True, exist_ok=True)
    STATUS_FILE.write_text(json.dumps(status, indent=2), encoding="utf-8")
    ok(f"Status written to {STATUS_FILE.relative_to(REPO_ROOT)}")

    # ------------------------------------------------------------------
    # Summary
    # ------------------------------------------------------------------
    print()
    if overall_ok:
        print(f"{BOLD}{GREEN}Import complete.{RESET}")
    else:
        print(f"{BOLD}{YELLOW}Import finished with warnings — check output above.{RESET}")

    if record_counts:
        print()
        print("  Dataset counts:")
        for name, count in record_counts.items():
            label = {"items": "Items", "monsters": "Monsters/NPCs", "prayers": "Prayers"}.get(name, name)
            print(f"    {label:<20} {count:>7,}")

    print()
    print("Next steps:")
    print("  1. Run /implement-skill-action-framework woodcutting")
    print("  2. Run /implement-tick-engine-core")
    print("  3. Run /combat-engine-full")
    print()

    return 0 if overall_ok else 1


if __name__ == "__main__":
    sys.exit(main())
