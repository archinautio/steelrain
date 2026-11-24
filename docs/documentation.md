# Maintaining the documentation

## Where each explanation belongs

Keep each explanation in one place and link to it elsewhere instead of copying prose:

- What a player needs to install, set up and play, the file formats players write, and the game
  rules belong in the public site under `docs/site/`. Write it for players: no class names, tests,
  build internals or design rationale.
- What a maintainer must preserve belongs in the other pages of `docs/` (for example
  [architecture](architecture.md), [unit files](unit-files.md), [color themes](color-themes.md)).
  Link to the site page that documents the player-facing side instead of restating it.
- `CLAUDE.md` holds only what every working session needs and links to these pages for the rest.
- Source comments state a declaration's contract briefly, or explain what the code cannot say for
  itself.

When you change something a player sees (a command, an option, a key, a file format, a rule),
update the site page that owns it in the same change.

Write every page for people to read: prefer a list or a table over a long paragraph of clauses
joined by semicolons.

The public site follows `main` and is built from `docs/site/` using Zensical, configured in
`zensical.toml`. Only the generated `build/site/` is uploaded to GitHub Pages.

## Preview locally

From the repository root, using Python 3.15:

```sh
python3 -m venv .venv-docs
source .venv-docs/bin/activate
python -m pip install -r requirements-docs.txt
zensical serve
```

On Windows, use `py -m venv .venv-docs` and `.venv-docs\Scripts\Activate.ps1` in PowerShell. Open
the local URL printed by Zensical. Stop the preview with Ctrl-C.

## Validate changes

```sh
zensical build --clean --strict
```

- Strict validation checks internal links and anchors.
- A new page must also be added to `nav` in `zensical.toml`.
- Check documented keys, commands and messages against the running game
  (`./gradlew :tui:shadowJar && java -jar tui/build/libs/tui.jar`) or against `Keybindings.kt` and
  `Cli.kt`.

Do not commit `build/site/`, `.cache/`, or the virtual environment.

## Screenshots

The images in `docs/site/assets/screenshots/` are generated, not taken by hand.
`scripts/docs-screenshots/take-screenshots.sh` plays scripted matches in a detached tmux session
and renders the terminal into PNGs with a Nerd Font, cropping each to the part of the screen the
page explains. Regenerate them all when the game's screens change:

```sh
python3 -m venv .venv-shots
.venv-shots/bin/python -m pip install -r scripts/docs-screenshots/requirements.txt
PYTHON=.venv-shots/bin/python scripts/docs-screenshots/take-screenshots.sh
```

- Needs `tmux`, JDK 25, and FiraCode Nerd Font Mono (in `~/Library/Fonts`, or set
  `DOCS_FONT_DIR`). It builds the game first if `tui/build/libs/tui.jar` is missing; rebuild it
  yourself to capture changes.
- The match uses `scripts/docs-screenshots/duel.json`, a unit collection with adjacent, skilled
  units so that attacks hit and physical attacks are possible.
- Dice rolls are random, so look over the new images before committing them.
- The host's network addresses are replaced with an example address before rendering.
- Crops are given in terminal cells; adjust them in the script when a layout changes.

Do not commit `.venv-shots/`.

## Publishing

`.github/workflows/docs.yml` builds the site on every pull request and push to `main`, and deploys
it to GitHub Pages from `main` only.

- Pull requests validate without deploying, and manual runs deploy only from `main`.
- A failed build leaves the previously deployed site in place.
- To recover from a bad content update, revert it through the normal review process; the next
  successful `main` build republishes the corrected site.
- The repository's Pages source must be set to **GitHub Actions** (Settings → Pages). No custom
  domain, deployment token or generated branch is needed.

Before publishing a larger change, check desktop and narrow layouts, search, and a directly opened
nested page under the `/steelrain/` prefix.
