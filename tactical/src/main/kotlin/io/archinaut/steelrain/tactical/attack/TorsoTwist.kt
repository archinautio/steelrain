package io.archinaut.steelrain.tactical.attack

import io.archinaut.steelrain.tactical.model.HexDirection

/**
 * The legal set of torso facings for a unit whose legs face [legFacing]: the
 * leg facing itself (no twist) or ±1 hexside either direction — a BattleTech
 * mech can twist its torso at most one hexside off its leg facing.
 *
 * Single source shared by the impulse-commit validation
 * ([ImpulseAttackPhaseHandler.validateTorsoFacings]) and the read-side
 * [io.archinaut.steelrain.tactical.query.PlayerView.legalTorsoFacings] query the TUI uses
 * to decide whether a twist input is legal before submitting.
 */
public fun torsoTwistOptions(legFacing: HexDirection): Set<HexDirection> =
    setOf(legFacing, legFacing.rotateClockwise(), legFacing.rotateCounterClockwise())
