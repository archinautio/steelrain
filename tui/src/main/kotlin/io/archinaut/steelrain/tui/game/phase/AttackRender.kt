package io.archinaut.steelrain.tui.game.phase

import io.archinaut.steelrain.tactical.attack.weapon.TargetInfo
import io.archinaut.steelrain.tactical.unit.UnitId

internal data class AttackRender(
    val targets: List<TargetInfo>,
    val weaponAssignments: Map<UnitId, Set<Int>>,
    val primaryTargetId: UnitId?,
    val cursorTargetIndex: Int,
    val cursorWeaponIndex: Int,
)
