#!/usr/bin/env bash
#
# update_remote_config.sh — publish the in-app update parameters and release notes
# to Firebase Remote Config.
#
# Usage:
#   ./update_remote_config.sh           # Publishes to Firebase Remote Config
#   ./update_remote_config.sh --dry-run # Validates and prepares template without deploying
#
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

PYTHON=""
for candidate in python3 python py; do
  if command -v "$candidate" >/dev/null 2>&1 && "$candidate" -c "import sys" >/dev/null 2>&1; then
    PYTHON="$candidate"
    break
  fi
done

if [ -z "$PYTHON" ]; then
  echo "ERROR: python3/python/py not found — a Python interpreter is required." >&2
  exit 1
fi

"$PYTHON" update_remote_config.py "$@"
