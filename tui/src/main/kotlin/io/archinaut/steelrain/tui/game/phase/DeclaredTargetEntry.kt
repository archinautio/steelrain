package io.archinaut.steelrain.tui.game.phase

import io.archinaut.steelrain.tactical.unit.UnitId

internal data class DeclaredTargetEntry(
    val targetId: UnitId,
    val isPrimary: Boolean,
    val weapons: List<DeclaredWeaponEntry>,
)
