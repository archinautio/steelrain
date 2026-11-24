# Unit collection files

The file format, validation rules a player sees, and command-line usage are documented for players
in [Custom maps, units and mechs](site/custom-content.md#unit-collections). This page holds only
what a maintainer must preserve.

- A unit file is only the immutable starting roster. Runtime state (armor damage, heat, ammunition,
  destroyed equipment) is constructed from each registered mech variant, never stored in the file.
- Validation is split in two:
  - **On read**, independent of any map or mech catalog: nonblank unique IDs, `player` 1 or 2,
    skills 0–8, unknown fields rejected. This is also what backs `--list-units`, so a broken file
    is caught by listing it, not only by launching it.
  - **At launch**, once `--map`/`--unit` (or `ContentCatalog.resolveGame`) pairs a roster with a
    map: the variant exists in the startup mech catalog, the position is inside the chosen map (the
    error names both the unit and the map), no shared positions, both players represented.
- External mech variants are registered before a unit collection is resolved, so a roster can
  reference them.
- Unlike maps and mechs, one unit file is selected wholesale: `--unit` never composes a roster
  from several registered files the way `--add-mech` composes variants from several collections.
- External names must not collide with each other or with a packaged collection; `unit/index.json`
  lists the packaged ones.
