package io.archinaut.steelrain.tactical.attack

import io.archinaut.steelrain.tactical.model.GameState
import io.archinaut.steelrain.tactical.model.HexDirection
import io.archinaut.steelrain.tactical.unit.UnitId

public fun GameState.applyTorsoFacings(facings: Map<UnitId, HexDirection>): GameState {
    if (facings.isEmpty()) return this
    return copy(
        units = units.mapUnits { unit ->
            val torso = facings[unit.id]
            if (torso != null) unit.copy(torsoFacing = torso) else unit
        },
    )
}

public fun GameState.resetTorsoFacings(): GameState =
    copy(
        units = units.mapUnits { unit ->
            if (unit.torsoFacing != unit.facing) unit.copy(torsoFacing = unit.facing) else unit
        },
    )
