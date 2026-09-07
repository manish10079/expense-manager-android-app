#!/usr/bin/env bash
#
# update_remote_config.sh — publish the in-app update parameters to Firebase
# Remote Config without losing any existing parameter.
#
#  1. Reads versionCode / versionName from app/build.gradle.kts
#  2. Pulls the CURRENT live template (`firebase remoteconfig:get`) so the
#     publish never drops parameters that already exist on the server
#  3. Overlays the update parameters from remote_config.json — edit
#     force_update / update_title / update_message there
#  4. Forces latest_version / latest_version_code to the app version
#  5. Publishes via `firebase remoteconfig:publish`
#
# Prerequisites:
#   - firebase CLI installed and logged in:
#       npm install -g firebase-tools
#       firebase login
#   - .firebaserc points at the project to publish to (default project is used)
#
# Usage (from the repo root):
#   ./update_remote_config.sh
#
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

BUILD_FILE="app/build.gradle.kts"
SEED_FILE="remote_config.json"
LIVE_FILE=".remote_config_live.json"
OUT_FILE="remote_config.json"

# --- 1. Read versionCode / versionName from app/build.gradle.kts ----------
VERSION_CODE="$(sed -n 's/.*versionCode[[:space:]]*=[[:space:]]*\([0-9][0-9]*\).*/\1/p' "$BUILD_FILE" | head -n 1)"
VERSION_NAME="$(sed -n 's/.*versionName[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$BUILD_FILE" | head -n 1)"

if [ -z "$VERSION_CODE" ] || [ -z "$VERSION_NAME" ]; then
  echo "ERROR: could not parse versionCode/versionName from $BUILD_FILE" >&2
  exit 1
fi
echo "App version: $VERSION_NAME (code $VERSION_CODE)"

# Pick a working Python interpreter (python3 / python / py). On Windows the
# 'python3' alias is often a Microsoft Store stub that just prints an error.
PYTHON=""
for candidate in python3 python py; do
  if command -v "$candidate" >/dev/null 2>&1 && "$candidate" -c "import sys" >/dev/null 2>&1; then
    PYTHON="$candidate"
    break
  fi
done
if [ -z "$PYTHON" ]; then
  echo "ERROR: python3/python/py not found — a Python interpreter is required to merge the JSON template." >&2
  exit 1
fi

if [ ! -f "$SEED_FILE" ]; then
  echo "ERROR: $SEED_FILE not found" >&2
  exit 1
fi

# --- 2. Pull the live template so existing params are preserved -----------
if firebase remoteconfig:get -o "$LIVE_FILE" >/dev/null 2>&1; then
  BASE_FILE="$LIVE_FILE"
  echo "Fetched current Remote Config template."
else
  BASE_FILE="$SEED_FILE"
  echo "WARNING: could not fetch the live template (not logged in? wrong project?);" >&2
  echo "         falling back to $SEED_FILE as the base template." >&2
fi

# --- 3 & 4. Merge update params + force version fields --------------------
"$PYTHON" - "$BASE_FILE" "$SEED_FILE" "$OUT_FILE" "$VERSION_CODE" "$VERSION_NAME" <<'PY'
import json
import sys

base_file, seed_file, out_file, vc, vn = (
    sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5]
)

with open(base_file, encoding="utf-8") as f:
    base = json.load(f)
with open(seed_file, encoding="utf-8") as f:
    seed = json.load(f)

# Metadata the publish API rejects — never send it back.
base.pop("version", None)
base.pop("etag", None)

params = base.setdefault("parameters", {})
seed_params = seed.get("parameters", {})

# Editable update params come from the committed seed...
for key in (
    "latest_version",
    "latest_version_code",
    "force_update",
    "update_title",
    "update_message",
):
    if key in seed_params:
        params[key] = seed_params[key]

# ...but the version fields are always forced from app/build.gradle.kts so
# they can never drift from the actual release.
latest_version = params.setdefault("latest_version", {}).setdefault("defaultValue", {})
latest_version["value"] = vn
latest_version_code = params.setdefault("latest_version_code", {}).setdefault("defaultValue", {})
latest_version_code["value"] = vc

with open(out_file, "w", encoding="utf-8") as f:
    json.dump(base, f, indent=2, ensure_ascii=False)
    f.write("\n")
PY

rm -f "$LIVE_FILE"

# --- 5. Publish ------------------------------------------------------------
# firebase-tools v15+ publishes via `deploy --only remoteconfig`, which reads
# the template path configured under "remoteconfig.template" in firebase.json
# (we use $OUT_FILE).
echo "Publishing Remote Config template from $OUT_FILE ..."
firebase deploy --only remoteconfig

echo "Done. Published latest_version=$VERSION_NAME, latest_version_code=$VERSION_CODE."