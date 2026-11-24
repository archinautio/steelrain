# Custom maps, units and mechs

You can add your own maps, mech designs, and unit collections (starting forces) as JSON files, and
register them on the command line:

| Content | Register with | Select with |
|---|---|---|
| [Map](#maps) | `--add-map <path>` | `--map <name>`, or the setup screen |
| [Mech collection](#mech-collections) | `--add-mech <path>` | Every mech is available at once |
| [Unit collection](#unit-collections) | `--add-unit <path>` | `--unit <name>` |

```sh
java -jar tui/build/libs/tui.jar \
  --add-map ./maps/arena.json \
  --add-mech ./mechs/custom.json \
  --add-unit ./units/duel.json \
  hot-seat --map arena --unit duel
```

Rules shared by every kind:

- A file is registered under its filename without `.json`. Names are exact and case-sensitive.
- The game refuses to start if two registered files have the same name, or if a registered name
  matches built-in content. Your files never replace built-in content.
- Unknown JSON fields are rejected, so a misspelled field is reported instead of silently ignored.
- Check a file by listing it, for example `--add-unit ./units/duel.json --list-units`; an invalid
  file is reported with the reason.

In a [network match](network.md), only the host needs the files: the joining player receives the
map and mechs from the host.

## Maps

A map gives its size and lists only the hexes that are not clear, flat ground.

```json
{
  "width": 10,
  "height": 10,
  "hexes": [
    { "col": 4, "row": 3, "terrain": "LIGHT_WOODS", "elevation": 1 },
    { "col": 5, "row": 4, "terrain": "HEAVY_WOODS" },
    { "col": 7, "row": 3, "terrain": "WATER", "depth": 2 },
    { "col": 6, "row": 2, "elevation": 3 },
    { "col": 2, "row": 7, "terrain": "ROUGH" }
  ]
}
```

| Field | Meaning | Default |
|---|---|---|
| `width`, `height` | Board size in hexes; both must be positive. | Required |
| `hexes[].col`, `hexes[].row` | Position, counted from 1 and within the board. | Required |
| `hexes[].terrain` | `CLEAR`, `LIGHT_WOODS`, `HEAVY_WOODS`, `WATER`, or `ROUGH`. | `CLEAR` |
| `hexes[].elevation` | Height of the hex. | `0` |
| `hexes[].depth` | Water depth. | `0` |

Every hex not listed is clear terrain at elevation 0. See [Movement](rules/movement.md),
[Line of sight](rules/line-of-sight.md) and [Water](rules/water.md) for how terrain affects play.

## Mech collections

A mech collection defines one or more mech designs. Registered mechs appear in the setup screen and
can be used in unit collections.

```json
{
  "models": [
    {
      "variant": "LCT-1V",
      "name": "Locust LCT-1V",
      "tonnage": 20,
      "walkingMP": 8,
      "runningMP": 12,
      "jumpMP": 0,
      "heatSinks": { "type": "STS", "units": 10 },
      "armor": {
        "head": 8,
        "centerTorso": 10,
        "centerTorsoRear": 2,
        "leftTorso": 8,
        "leftTorsoRear": 2,
        "rightTorso": 8,
        "rightTorsoRear": 2,
        "leftArm": 4,
        "rightArm": 4,
        "leftLeg": 4,
        "rightLeg": 4
      },
      "loadout": [
        { "type": "weapon", "location": "CENTER_TORSO", "weapon": "mediumLaser" },
        { "type": "ammo", "location": "CENTER_TORSO", "ammo": "MG", "tons": 1 }
      ]
    }
  ]
}
```

| Field | Meaning | Default |
|---|---|---|
| `variant` | Unique identifier, for example `LCT-1V`. It may appear only once across all built-in and registered collections. | Required |
| `name` | Display name. | Required |
| `tonnage` | Weight in tons. | Required |
| `walkingMP`, `runningMP` | Walking and running movement points. | Required |
| `jumpMP` | Jumping movement points. | `0` |
| `heatSinks` | Heat sink `type` (`STS` for single, `DTS` for double) and number of `units` (at least 10). | 10 single heat sinks |
| `armor` | Armor points for every location, including the three rear torso locations. All are required. | Required |
| `loadout` | Equipment, applied in order. | Empty |

Internal structure is worked out from the tonnage (see
[Armor and damage](rules/armor-damage.md)). After the loadout is placed, heat sinks beyond the
first 10 and one jump jet per jump MP are added automatically. Everything must fit in the mech's
critical slots, or the file is rejected (see [Critical hits](rules/critical-hits.md)).

### Loadout entries

| `type` | Fields |
|---|---|
| `weapon` | `location` and `weapon` |
| `ammo` | `location`, `ammo`, and optional `tons` (positive, default 1) |
| `omitActuators` | An arm `location` and `lowerArm`, `hand`, or both. Place it before equipment that needs the freed slots. |

| Value | Allowed values |
|---|---|
| `location` | `HEAD`, `CENTER_TORSO`, `LEFT_TORSO`, `RIGHT_TORSO`, `LEFT_ARM`, `RIGHT_ARM`, `LEFT_LEG`, `RIGHT_LEG` |
| `weapon` | `smallLaser`, `mediumLaser`, `largeLaser`, `ppc`, `machineGun`, `ac5`, `ac20`, `srm2`, `srm6`, `lrm5`, `lrm10`, `lrm20` |
| `ammo` | `MG`, `AC5`, `AC20`, `SRM2`, `SRM6`, `LRM5`, `LRM10`, `LRM20` |

## Unit collections

A unit collection is a fixed starting force for both players: which mechs, where they start, which
way they face, and their pilots' skills. It has no map of its own, so the same collection can be
played on any map it fits.

```json
{
  "units": [
    {
      "id": "A1",
      "player": 1,
      "variant": "AS7-D",
      "gunnerySkill": 4,
      "pilotingSkill": 5,
      "position": { "col": 2, "row": 2 },
      "facing": "SE"
    },
    {
      "id": "W1",
      "player": 2,
      "variant": "WVR-6R",
      "gunnerySkill": 4,
      "pilotingSkill": 5,
      "position": { "col": 14, "row": 16 },
      "facing": "NW"
    }
  ]
}
```

| Field | Meaning |
|---|---|
| `id` | Unit name shown in the game; not blank and unique within the file. |
| `player` | `1` or `2`. |
| `variant` | A mech variant from a built-in or registered mech collection. |
| `gunnerySkill`, `pilotingSkill` | Pilot skills from 0 to 8; lower is better. |
| `position` | Starting hex, counted from 1. |
| `facing` | `N`, `NE`, `SE`, `S`, `SW`, or `NW`. |

When the match starts, the collection is checked against the chosen map and mechs:

- every variant must exist;
- every position must be on the map, so a collection made for a large map may not fit a small one;
- no two units may share a hex;
- both players must have at least one unit.

Unit collections are used only when starting a match from the command line (`hot-seat`, `host`,
`server`); the setup screen places mechs automatically. The built-in collection is `default`.
