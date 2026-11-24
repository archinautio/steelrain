package io.archinaut.steelrain.tui.game.phase

import io.archinaut.steelrain.tactical.model.PlayerId
import io.archinaut.steelrain.tactical.unit.UnitId

internal data class DeclaredAttackerEntry(
    val attackerId: UnitId,
    val ownerPlayer: PlayerId,
    val isDraft: Boolean,
    val targets: List<DeclaredTargetEntry>,
)
