package io.archinaut.steelrain.tui.game

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.HexDirection

public data class FacingSelection(
    val hex: HexCoordinates,
    val facings: Set<HexDirection>,
)
