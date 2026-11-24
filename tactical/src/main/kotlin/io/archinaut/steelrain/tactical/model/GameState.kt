package io.archinaut.steelrain.tactical.model

import io.archinaut.steelrain.tactical.unit.CombatUnit
import io.archinaut.steelrain.tactical.unit.UnitRoster
import kotlinx.serialization.Serializable

@Serializable
public data class GameState(
    public val units: UnitRoster<CombatUnit>,
    public val map: GameMap,
)
