package io.archinaut.steelrain.tactical.unit

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.HexDirection
import io.archinaut.steelrain.tactical.model.PlayerId

public fun MechModel.createUnit(
    id: UnitId,
    owner: PlayerId,
    gunnerySkill: Int = 4,
    pilotingSkill: Int = 5,
    position: HexCoordinates,
    facing: HexDirection = HexDirection.N,
): CombatUnit = CombatUnit(
    model = this,
    id = id,
    owner = owner,
    gunnerySkill = gunnerySkill,
    pilotingSkill = pilotingSkill,
    position = position,
    facing = facing,
)
