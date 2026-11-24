package io.archinaut.steelrain.tactical.movement

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.HexDirection
import kotlinx.serialization.Serializable

@Serializable
public data class ReachableHex(
    public val position: HexCoordinates,
    public val facing: HexDirection,
    public val mpSpent: Int,
    public val path: List<MovementStep>,
)
