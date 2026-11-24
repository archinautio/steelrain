# Mech model collection files

The file format, loadout entries, and identifiers are documented for players in
[Custom maps, units and mechs](site/custom-content.md#mech-collections). This page holds only what a
maintainer must preserve.

- Packaged models live under `mech/`; `mech/index.json` names every packaged collection loaded at
  startup.
- A variant may occur only once across all packaged and external collections. Duplicate definitions
  fail startup even when their contents are identical; external files never override packaged
  content.
- Weapon identifiers (`mediumLaser`, `ac20`, …) are stable file-format values: renaming one breaks
  every existing collection file. Locations and ammo types use the uppercase enum spellings already
  used by game and engine JSON.
- Internal structure is derived from tonnage. After the ordered loadout is applied, heat sinks
  beyond the first 10 and one jump jet per jump MP are placed using the engine's standard scan
  order; the resulting critical layout must fit and validate.
- A joining client needs no local model files: the host includes the match's definitions in the
  same bootstrap as the map and initial state, and the client installs them before decoding compact
  model references in that state. `join` still accepts `--add-mech`: after joining, each host
  definition whose variant also exists in the client's registered catalog is compared structurally
  with the local definition. A difference adds a warning to the local game log, but the host
  definition remains authoritative; a variant unavailable locally is accepted silently.
