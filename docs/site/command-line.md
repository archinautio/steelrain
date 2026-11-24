# Command line

```text
battletech [<options>] [<command> [<command options>]]
```

`battletech` stands for the way you start the game; see [Download](index.md#download) for the
jar and from-source equivalents.

With no command, the game opens the [setup screen](setup.md). A command starts a match directly:

| Command | What it does |
|---|---|
| `hot-seat` | Both players share this terminal. |
| `host` | Host a network match; the other player connects with `join`. |
| `join <address>` | Join a network match. |
| `server` | Run a dedicated server with no game screen; both players connect with `join`. |

Run `--help` on its own, or after a command, for the full option list:

```sh
battletech --help
battletech host --help
```

!!! warning "Options go before the command"
    The general options below must come **before** the command name. The command's own options
    come after it:

    ```sh
    battletech --add-map arena.json hot-seat --map arena
    ```

## General options

These work with every command, and with no command.

| Option | Meaning |
|---|---|
| `--add-map <path>` | Register a map file. May be repeated. |
| `--add-mech <path>` | Register a mech collection file. May be repeated. |
| `--add-unit <path>` | Register a unit collection file. May be repeated. |
| `--theme <name>` or `--theme <path>` | Use a built-in color theme, or a theme file. See [Color themes](themes.md). |
| `--list-maps` | List the available maps, then exit. |
| `--list-mechs` | List the available mechs with tonnage and movement points, then exit. |
| `--list-units` | List the available unit collections and their units, then exit. |
| `--list-themes` | List the available color themes, then exit. |
| `--version` | Show the version, the license and the trademark notice, then exit. |

A registered file is named after its filename without `.json`: `--add-map ./maps/arena.json`
registers the map `arena`. Listings include built-in and registered content, marking registered
entries `(external)`. Several `--list-*` options can be combined:

```sh
battletech --add-map ./maps/arena.json --list-maps --list-units
```

See [Custom maps, units and mechs](custom-content.md) for the file formats.

## `hot-seat`, `host`, and `server`

| Option | Meaning | Default |
|---|---|---|
| `--map <name>` | The map to play on. | `battletech-classic` |
| `--unit <name>` | The unit collection to play. | `default` |
| `--port <number>` | TCP port to listen on (`host` and `server` only). | `2470` |

`--map` and `--unit` choose from the built-in content plus anything registered with `--add-map`
and `--add-unit`. A unit collection has no map of its own, so the same collection can be played on
any map large enough for its starting positions.

```sh
battletech --add-unit ./units/duel.json hot-seat --map river-valley --unit duel
```

## `join`

```text
join <address>[:<port>] --session <id>
```

| Argument | Meaning |
|---|---|
| `<address>` | The host's IP address or name, optionally followed by `:port` (default `2470`). |
| `--session <id>` | The session ID the host shows. Required. |

`join` takes no `--map` or `--unit`; the host chooses them. See [Network play](network.md).

## Built-in content

| Kind | Names |
|---|---|
| Maps | `battletech-classic`, `lake-area`, `river-valley`, `test` |
| Unit collections | `default` |
| Mech collections | `classic` |
| Themes | `dark`, `light`, `dark-256`, `light-256`, `dark-16`, `light-16` |

Use the `--list-*` options to see what each contains.
