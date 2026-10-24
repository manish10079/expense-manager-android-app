#!/usr/bin/env python3
"""
update_remote_config.py — Publish latest release notes and version to Firebase Remote Config.

Usage:
    python update_remote_config.py [--dry-run]
    ./update_remote_config.ps1     [--dry-run]
    ./update_remote_config.sh      [--dry-run]

What it does:
  1. Reads versionCode and versionName from app/build.gradle.kts
  2. Reads localized update_message from whats_new.json
  3. Pulls the current live template (`firebase remoteconfig:get`) so other parameters
     (like ad_pass_duration_minutes, gemini_voice_model, etc.) are never lost
  4. Updates:
       - latest_version: set to versionName from build.gradle.kts
       - update_message: set to localized JSON string from whats_new.json
       - force_update: set from remote_config.json
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


def load_whats_new(version_code, version_name):
    if not WHATS_NEW_FILE.exists():
        sys.exit(f"ERROR: {WHATS_NEW_FILE} not found.")

    with open(WHATS_NEW_FILE, "r", encoding="utf-8") as f:
        data = json.load(f)

    wn_vc = data.get("versionCode")
    wn_vn = data.get("versionName")

    if wn_vc != version_code or wn_vn != version_name:
        print(f"[INFO] Syncing whats_new.json version ({wn_vn}({wn_vc}) -> {version_name}({version_code}))")
        data["versionCode"] = version_code
        data["versionName"] = version_name
        with open(WHATS_NEW_FILE, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

    update_message = data.get("update_message")
    if not update_message:
        sys.exit("ERROR: whats_new.json is missing 'update_message' section.")
    if not isinstance(update_message, dict):
        sys.exit("ERROR: update_message must be a JSON object keyed by language (en, hi, …).")

    missing = [k for k in APP_UPDATE_MESSAGE_KEYS if k not in update_message]
    extra = [k for k in update_message if k not in APP_UPDATE_MESSAGE_KEYS]
    if missing:
        sys.exit(f"ERROR: update_message missing app language keys: {', '.join(missing)}")
    if extra:
        print(f"[WARN] update_message has extra keys the app ignores: {', '.join(extra)}")
    for key, value in update_message.items():
        if not isinstance(value, str) or not value.strip():
            sys.exit(
                f"ERROR: update_message[{key}] must be a non-empty string "
                "(flat locale map, not nested title/message)."
            )

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

    update_message = load_whats_new(version_code, version_name)
    langs = list(update_message.keys()) if isinstance(update_message, dict) else ["plain"]
    print(f"[INFO] Localized languages in update_message ({len(langs)}): {', '.join(langs)}")

    # Format update_message for Remote Config value
    if isinstance(update_message, dict):
        update_message_val = json.dumps(update_message, indent=2, ensure_ascii=False)
    else:
        update_message_val = str(update_message)

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
