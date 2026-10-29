#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if ! command -v mvn >/dev/null 2>&1; then
  echo 'Install Maven 3.9+ then run: mvn javafx:run' >&2
  exit 1
fi
mvn -q javafx:run
