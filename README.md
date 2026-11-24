<img src="docs/site/assets/battletech-logo.svg" alt="BattleTech logo: a mech head inside a hex tile, its visor a terminal prompt" width="128" height="128">

# BattleTech

A multi-module Kotlin project to play BattleTech tabletop game in a terminal (TUI).

The TUI consumes [Tenter](https://github.com/archinautio/tenter) (`io.archinaut:tenter`) as a library resolved from Maven Central.

Supports hot-seat and network play.

- [User documentation](https://alejandropg.github.io/battletech/) — installation, setup, controls,
  network play, custom content, and the game rules.
- [Documentation source](docs/site/index.md) — read the player guides in this checkout.
- [Architecture](docs/architecture.md) — module boundaries and implementation invariants.
- [Documentation maintenance](docs/documentation.md) — preview, validation, and publishing.

## Quick start

Requires JDK 25 and a terminal using a [Nerd Font](https://www.nerdfonts.com/).

```sh
./gradlew :tui:shadowJar
java -jar tui/build/libs/tui.jar hot-seat
```

With no command the game opens the interactive setup screen. See the
[command line reference](docs/site/command-line.md) for `hot-seat`, `host`, `join`, and `server`.
