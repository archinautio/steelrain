package io.archinaut.steelrain.tactical.attack

import io.archinaut.steelrain.tactical.unit.CombatUnit

/** Result of resolving damage at one or more locations via transfer. */
public data class DamageResolution(
    val unit: CombatUnit,
    val steps: List<LocationDamage>,
)
