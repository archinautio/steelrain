package io.archinaut.steelrain.tui.game.phase

import io.archinaut.steelrain.tactical.attack.AttackResult
import io.archinaut.steelrain.tactical.model.PlayerId
import io.archinaut.steelrain.tactical.unit.UnitRoster
import io.archinaut.steelrain.tactical.unit.VisibleUnit

internal data class AttackResultsRender(
    val results: List<AttackResult>,
    val units: UnitRoster<VisibleUnit>,
    /** Who this render is for — lets [io.archinaut.steelrain.tui.view.AttackResultsView] tell an own
     *  attacker from a foreign one (`units.byId(id).owner == viewer`) without re-deriving it. */
    val viewer: PlayerId,
)
