#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if ! command -v groovy >/dev/null 2>&1; then
  echo "Fehler: Groovy ist nicht installiert. Installiere Groovy oder nutze 'groovy exfat.groovy'." >&2
  exit 1
fi

exec groovy "${SCRIPT_DIR}/exfat.groovy" "$@"
