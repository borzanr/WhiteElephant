#!/usr/bin/env bash
# Regera os assets da Play Store a partir dos HTMLs em src/.
# Uso: bash playstore/render.sh
set -euo pipefail

CHROME="${CHROME:-/c/Program Files (x86)/Google/Chrome/Application/chrome.exe}"
[ -f "$CHROME" ] || CHROME="/c/Program Files/Google/Chrome/Application/chrome.exe"
[ -f "$CHROME" ] || { echo "Chrome não encontrado; defina CHROME=caminho"; exit 1; }

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WIN_DIR="$(cd "$HERE" && pwd -W 2>/dev/null || echo "$HERE")"
URL="file:///${WIN_DIR}/src"
OUT="$(echo "$WIN_DIR" | tr '/' '\')"

shot() { # shot <url> <arquivo-saida> <largura> <altura>
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars \
    --force-device-scale-factor=1 --virtual-time-budget=3000 \
    --allow-file-access-from-files \
    --screenshot="$2" --window-size="$3,$4" "$1" > /dev/null 2>&1
  echo "gerado: $2"
}

for lang in en pt es; do
  mkdir -p "$HERE/screenshots-$lang"
  shot "$URL/feature.html?lang=$lang" "${OUT}\feature-graphic-$lang.png" 1024 500
  for n in 1 2 3 4 5 6; do
    shot "$URL/shot.html?n=$n&lang=$lang" "${OUT}\screenshots-$lang\0$n.png" 1080 1920
  done
done
