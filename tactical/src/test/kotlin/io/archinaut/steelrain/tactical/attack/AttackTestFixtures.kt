package io.archinaut.steelrain.tactical.attack

import io.archinaut.steelrain.tactical.model.GameState
import io.archinaut.steelrain.tactical.query.aGameState
import io.archinaut.steelrain.tactical.query.aUnit
import io.archinaut.steelrain.tactical.query.aWeapon
import io.archinaut.steelrain.tactical.unit.CombatUnit
import io.archinaut.steelrain.tactical.unit.Weapon

/**
 * Builds a [WeaponAttackContext] from full [CombatUnit]s / [GameState], mirroring what the
 * authoritative handlers do: the target is passed as-is (the rules only ever read its public
 * projection — see [AttackContext], and [CombatUnit] is a [io.archinaut.steelrain.tactical.unit.VisibleUnit])
 * and only the map is carried over. Tests keep expressing fixtures in terms of whole units and
 * game state; the projection detail stays here rather than being restated at every call site.
 */
internal fun aWeaponAttackContext(
    actor: CombatUnit = aUnit(),
    gameState: GameState = aGameState(),
    target: CombatUnit = aUnit(id = "target"),
    weapon: Weapon = aWeapon(),
): WeaponAttackContext = WeaponAttackContext(
    actor = actor,
    map = gameState.map,
    target = target,
    weapon = weapon,
)

/** Physical counterpart of [aWeaponAttackContext]; same passthrough rationale. */
internal fun aPhysicalAttackContext(
    actor: CombatUnit = aUnit(),
    gameState: GameState = aGameState(),
    target: CombatUnit = aUnit(id = "target"),
): PhysicalAttackContext = PhysicalAttackContext(
    actor = actor,
    map = gameState.map,
    target = target,
)
