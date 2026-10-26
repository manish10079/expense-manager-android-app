#!/usr/bin/env python3
"""
update_remote_config.py — Publish latest release notes and version to Firebase Remote Config.

Usage:
    python update_remote_config.py [--dry-run]
    ./update_remote_config.ps1     [--dry-run]
    ./update_remote_config.sh      [--dry-run]

What it does:
  1. Reads versionCode and versionName directly from app/build.gradle.kts
  2. Reads localized update_message directly from whats_new.json
     (strips any redundant 'What's new' header so it displays clean bullet points)
  3. Pulls the current live template (`firebase remoteconfig:get`) so other parameters
     (like ad_pass_duration_minutes, min_required_version, etc.) are never lost
  4. Updates:
       - latest_version: set to versionName from build.gradle.kts
       - latest_version_code: set to versionCode from build.gradle.kts
       - update_message: set to localized JSON string from whats_new.json
       - force_update: set from remote_config.json if defined
       - removes obsolete params (e.g. update_title)
  5. Deploys to Firebase via `firebase deploy --only remoteconfig` (unless --dry-run)
"""

import argparse
import json
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent
BUILD_FILE = ROOT_DIR / "app" / "build.gradle.kts"
WHATS_NEW_FILE = ROOT_DIR / "whats_new.json"
SEED_FILE = ROOT_DIR / "remote_config.json"
LIVE_FILE = ROOT_DIR / ".remote_config_live.json"

# Must match supportedAppLanguages in AppLanguages.kt (not "system").
# App lookup: normalizeLanguageCode then tag → language → "en".
# Indonesian is "id" here ("in" is remapped in the app).
APP_UPDATE_MESSAGE_KEYS = (
    "en",
    "or",
    "hi",
    "kn",
    "ta",
    "bn",
    "mr",
    "ml",
    "te",
    "as",
    "pa",
    "gu",
    "es",
    "pt-BR",
    "fr",
    "de",
    "id",
    "ja",
    "ko",
    "it",
    "ru",
)

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")


def parse_app_version():
    if not BUILD_FILE.exists():
        sys.exit(f"ERROR: {BUILD_FILE} not found.")

    content = BUILD_FILE.read_text(encoding="utf-8")
    vc_match = re.search(r"versionCode\s*=\s*(\d+)", content)
    vn_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)

    if not vc_match or not vn_match:
        sys.exit("ERROR: Could not parse versionCode or versionName from app/build.gradle.kts")

    return int(vc_match.group(1)), vn_match.group(1)


def strip_whats_new_header(text: str) -> str:
    """Removes any redundant 'What's New in X.X:' heading so messages start cleanly with bullet points."""
    trimmed = text.strip()
    if "•" in trimmed and not trimmed.startswith("•"):
        return "•" + trimmed.split("•", 1)[1]
    return trimmed


def load_whats_new():
    if not WHATS_NEW_FILE.exists():
        sys.exit(f"ERROR: {WHATS_NEW_FILE} not found.")

    with open(WHATS_NEW_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    # Support either flat locale mapping {"en": "...", ...} or nested {"update_message": {"en": "...", ...}}
    if isinstance(data, dict) and "update_message" in data and isinstance(data["update_message"], dict):
        raw_message = data["update_message"]
    elif isinstance(data, dict):
        # Filter out any metadata keys if present, keeping language keys
        raw_message = {k: v for k, v in data.items() if k not in ("versionCode", "versionName", "play_store_whats_new")}
    else:
        sys.exit("ERROR: whats_new.json must be a JSON object containing language keys (en, hi, …).")

    missing = [k for k in APP_UPDATE_MESSAGE_KEYS if k not in raw_message]
    extra = [k for k in raw_message if k not in APP_UPDATE_MESSAGE_KEYS]
    if missing:
        sys.exit(f"ERROR: whats_new.json missing app language keys: {', '.join(missing)}")
    if extra:
        print(f"[WARN] whats_new.json has extra keys the app ignores: {', '.join(extra)}")

    update_message = {}
    for key, value in raw_message.items():
        if not isinstance(value, str) or not value.strip():
            sys.exit(f"ERROR: whats_new.json[{key}] must be a non-empty string.")
        update_message[key] = strip_whats_new_header(value)

    return update_message


def run_cmd(cmd, check=True):
    # Use shell=True on Windows to resolve PATH commands like firebase.cmd
    return subprocess.run(cmd, check=check, cwd=ROOT_DIR, shell=True)


def main():
    parser = argparse.ArgumentParser(description="Publish update metadata to Firebase Remote Config.")
    parser.add_argument("--dry-run", action="store_true", help="Merge and validate template without deploying.")
    args = parser.parse_args()

    version_code, version_name = parse_app_version()
    print(f"[INFO] Release version: {version_name} (code {version_code})")

    update_message = load_whats_new()
    langs = list(update_message.keys())
    print(f"[INFO] Localized languages in update_message ({len(langs)}): {', '.join(langs)}")

    # Format update_message for Remote Config value
    update_message_val = json.dumps(update_message, indent=2, ensure_ascii=False)

    # Fetch live template
    print("[INFO] Fetching current live Remote Config template...")
    res = run_cmd(f"firebase remoteconfig:get -o {LIVE_FILE.name}", check=False)
    if res.returncode == 0 and LIVE_FILE.exists():
        print("[SUCCESS] Fetched current Remote Config template from Firebase.")
        base_file = LIVE_FILE
    else:
        print("[WARN] Could not fetch live template; falling back to local remote_config.json.")
        base_file = SEED_FILE

    with open(base_file, "r", encoding="utf-8") as f:
        config = json.load(f)

    # Cleanup metadata rejected by publish API
    config.pop("version", None)
    config.pop("etag", None)

    params = config.setdefault("parameters", {})

    # 1. Update latest_version and latest_version_code
    latest_ver = params.setdefault("latest_version", {}).setdefault("defaultValue", {})
    latest_ver["value"] = version_name
    params["latest_version"]["valueType"] = "STRING"

    if "latest_version_code" in params:
        params["latest_version_code"].setdefault("defaultValue", {})["value"] = str(version_code)
        params["latest_version_code"]["valueType"] = "STRING"

    # 2. Update update_message with localized JSON string
    msg_param = params.setdefault("update_message", {}).setdefault("defaultValue", {})
    msg_param["value"] = update_message_val
    params["update_message"]["valueType"] = "STRING"

    # 3. Read force_update from remote_config.json if available
    if SEED_FILE.exists():
        with open(SEED_FILE, "r", encoding="utf-8") as f:
            seed = json.load(f)
        seed_params = seed.get("parameters", {})
        if "force_update" in seed_params:
            params["force_update"] = seed_params["force_update"]

    # 4. Remove obsolete parameters (update_title is now 100% offline in strings.xml)
    if "update_title" in params:
        print("[INFO] Removing obsolete 'update_title' parameter from Remote Config...")
        params.pop("update_title", None)

    # Write merged configuration to remote_config.json
    with open(SEED_FILE, "w", encoding="utf-8") as f:
        json.dump(config, f, indent=2, ensure_ascii=False)
        f.write("\n")

    # Clean up temporary live file
    if LIVE_FILE.exists():
        LIVE_FILE.unlink()

    print(f"[SUCCESS] Updated {SEED_FILE.name} with latest_version={version_name} and localized update_message.")

    if args.dry_run:
        print("[DRY RUN] Template prepared successfully. Skipping firebase deploy.")
        return

    # Deploy to Firebase Remote Config
    print("[INFO] Publishing Remote Config template to Firebase...")
    deploy_res = run_cmd("firebase deploy --only remoteconfig", check=False)
    if deploy_res.returncode != 0:
        sys.exit("[ERROR] firebase deploy failed.")

    print(f"[SUCCESS] Published latest_version={version_name} and multilingual update_message to Firebase!")


if __name__ == "__main__":
    main()
