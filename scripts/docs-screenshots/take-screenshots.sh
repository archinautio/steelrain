#!/usr/bin/env bash
# Plays scripted matches in a detached tmux session and renders the screens used by the public
# documentation site into docs/site/assets/screenshots/. See docs/documentation.md#screenshots.
#
# Needs: tmux, JDK 25, FiraCode Nerd Font Mono (see ansi2png.py), and a Python with the packages in
# requirements.txt (PYTHON, default: python3). Dice rolls are random, so each run differs slightly.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HERE="$ROOT/scripts/docs-screenshots"
OUT="$ROOT/docs/site/assets/screenshots"
PYTHON="${PYTHON:-python3}"
JAR="$ROOT/tui/build/libs/tui.jar"
WORK="$(mktemp -d)"
SESSION="bt-docs-shots"
COLS=130
ROWS=46

trap 'tmux kill-session -t "$SESSION" 2>/dev/null || true; rm -rf "$WORK"' EXIT
mkdir -p "$OUT"
rm -f "$OUT"/*.png
[[ -f "$JAR" ]] || (cd "$ROOT" && ./gradlew -q :tui:shadowJar)

# start <cols> <rows> <game args...>: runs the game in a fresh truecolor tmux pane.
start() {
  COLS=$1
  ROWS=$2
  shift 2
  tmux kill-session -t "$SESSION" 2>/dev/null || true
  tmux new-session -d -s "$SESSION" -x "$COLS" -y "$ROWS" -e COLORTERM=truecolor
  tmux set -t "$SESSION" -ga terminal-overrides ",*:RGB"
  tmux send-keys -t "$SESSION" "cd '$ROOT' && COLORTERM=truecolor java -jar '$JAR' $*" Enter
  sleep 4
}

# keys <key>...: sends each key, pausing so the game renders between them.
keys() {
  for key in "$@"; do
    tmux send-keys -t "$SESSION" "$key"
    sleep 0.4
  done
  sleep 0.6
}

banner() { tmux capture-pane -t "$SESSION" -p | sed -n 2p; }
unit_in_banner() { banner | grep -oE '\b[A-Z][0-9]:' | head -1 | tr -d ':' || true; }

# shot <name> [x0 y0 x1 y1]: renders the current screen, or a crop of it in cells, to OUT/<name>.png.
shot() {
  local name=$1
  shift
  tmux capture-pane -t "$SESSION" -pe > "$WORK/$name.ans"
  # Never publish this machine's network addresses (shown by the host's join commands).
  # The example has the same length as the real address, so panel borders stay aligned.
  perl -pi -e 'my @ex = ("", "", "", "", "", "", "", "1.2.3.4", "10.0.0.5", "10.0.0.20", "10.0.0.200",
      "10.0.10.200", "192.168.1.20", "192.168.1.200", "192.168.10.200", "192.168.100.200");
    s/\b(\d{1,3}(?:\.\d{1,3}){3})\b/$ex[length $1]/ge' "$WORK/$name.ans"
  "$PYTHON" "$HERE/ansi2png.py" "$WORK/$name.ans" "$OUT/$name.png" "$COLS" "$ROWS" "$@"
}

DUEL=(--add-unit scripts/docs-screenshots/duel.json)
MAP=(0 3 70 34)               # the tactical map panel, without the banner

# --- Setup screen -------------------------------------------------------------------------------
start 110 40
shot setup-mode 0 0 110 17
keys c ' '                    # lock hot-seat, select the map under the cursor
keys 3 ' ' d d s ' '          # Player 1: 3 x LCT-1V, 1 x STG-3R
keys +
shot setup-player 0 9 110 31
keys - 2 +
shot setup-map-preview 0 9 110 31

start 130 40                  # wider, so the four panels show full mech names
keys c ' ' 3 ' ' d d s ' ' 4 s s s s s s ' ' 2
shot setup-panels 0 9 130 26

# --- Host panel ---------------------------------------------------------------------------------
start 180 40                  # wide enough for the join command to fit the panel
keys s ' ' c
shot host-panel 0 10 46 22

# --- A match: movement, weapon and physical attacks --------------------------------------------
start 130 46 "${DUEL[@]}" hot-seat --unit duel
keys Tab                      # select a unit, so UNIT STATUS and its reachable hexes show
shot game-overview
keys Escape

guard=0
while banner | grep -q MOVEMENT && (( guard++ < 20 )); do
  keys Tab
  case "$(unit_in_banner)" in
    H1)
      shot move-walk "${MAP[@]}"
      keys x
      shot move-run "${MAP[@]}"
      keys e Enter
      shot move-facing "${MAP[@]}"
      keys e
      ;;
    A1) keys S ;;
    W1) keys W ;;
    L1) keys Q ;;
    *) keys Escape ;;
  esac
done

guard=0
while banner | grep -q "WEAPON ATTACK" && (( guard++ < 20 )); do
  keys Tab
  unit="$(unit_in_banner)"
  keys ' ' s ' ' s ' ' s ' '
  if [[ "$unit" == A1 ]]; then shot attack-targets 0 3 84 24; fi
  keys c
done
sleep 10                      # let the weapon-fire animations finish
shot attack-results 0 3 74 "$ROWS"

guard=0
while banner | grep -q "PHYSICAL ATTACK" && (( guard++ < 20 )); do
  keys Tab
  unit="$(unit_in_banner)"
  if [[ -n "$unit" ]]; then
    keys ' '
    if [[ "$unit" == A1 ]]; then shot physical-targets 0 0 84 16; fi
  fi
  keys c
done
sleep 6
shot log 102 3 130 "$ROWS"

keys '?'
shot help 88 3 130 24
keys '?' 1 +
shot record-sheet 0 3 62 41

# --- Color themes: the same corner of the map in four themes -----------------------------------
for theme in dark light dark-256 dark-16; do
  start 130 46 "${DUEL[@]}" --theme "$theme" hot-seat --unit duel
  # Draw the terminal-default cells in the theme's own colors, as a matching terminal would.
  read -r DOCS_DEFAULT_BG DOCS_DEFAULT_FG < <("$PYTHON" -c '
import json, sys
t = json.load(open(sys.argv[1]))
hexed = lambda v: v.lstrip("#") if v.startswith("#") else ""
print(hexed(t["background"]) or "101418", hexed(t["standard"]["DEFAULT"]) or "DDE2E5")
' "$ROOT/theme/$theme.json")
  DOCS_DEFAULT_BG=$DOCS_DEFAULT_BG DOCS_DEFAULT_FG=$DOCS_DEFAULT_FG shot "theme-$theme" 2 5 47 34
done
"$PYTHON" - "$OUT" <<'EOF'
import sys
from PIL import Image
out = sys.argv[1]
names = ["dark", "light", "dark-256", "dark-16"]
tiles = [Image.open(f"{out}/theme-{n}.png") for n in names]
w, h = tiles[0].size
grid = Image.new("RGBA", (2 * w + 8, 2 * h + 8), (0, 0, 0, 0))
for i, tile in enumerate(tiles):
    grid.paste(tile, ((i % 2) * (w + 8), (i // 2) * (h + 8)))
grid.save(f"{out}/themes.png")
EOF
rm -f "$OUT"/theme-*.png

echo "Screenshots written to $OUT"
