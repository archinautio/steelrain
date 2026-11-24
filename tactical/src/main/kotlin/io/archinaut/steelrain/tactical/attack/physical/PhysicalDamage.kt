package io.archinaut.steelrain.tactical.attack.physical

import io.archinaut.steelrain.tactical.unit.CombatUnit
import kotlin.math.ceil

/** Punch damage (`docs/site/rules/physical-attacks.md` §5). */
public fun punchDamage(actor: CombatUnit): Int = ceil(actor.tonnage / 10.0).toInt()

/** Kick damage (`docs/site/rules/physical-attacks.md` §5). */
public fun kickDamage(actor: CombatUnit): Int = ceil(actor.tonnage / 5.0).toInt()
