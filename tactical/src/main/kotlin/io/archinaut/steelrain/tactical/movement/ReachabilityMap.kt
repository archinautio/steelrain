package io.archinaut.steelrain.tactical.movement

import io.archinaut.steelrain.tactical.model.MovementMode

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.HexDirection

public data class ReachabilityMap(
    public val mode: MovementMode,
    public val maxMP: Int,
    public val destinations: List<ReachableHex>,
) {
    public fun facingsByPosition(): Map<HexCoordinates, Set<HexDirection>> =
        destinations
            .groupBy { it.position }
            .mapValues { (_, hexes) -> hexes.map { it.facing }.toSet() }
}
