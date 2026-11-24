package io.archinaut.steelrain.tactical.attack.weapon

import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.attack.WeaponAttackContext
import io.archinaut.steelrain.tactical.model.unitWaterDepth
import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.rules.RuleRejection

/**
 * Blocks weapon fire when the **attacker** is fully submerged and the weapon is not
 * [io.archinaut.steelrain.tactical.unit.Weapon.underwaterCapable] (`docs/site/rules/water.md` §2).
 *
 * All weapons in [io.archinaut.steelrain.tactical.unit.WeaponModels] default to
 * `underwaterCapable = false`, so fire is blocked entirely for submerged units unless a
 * scenario-specific weapon model explicitly opts in.
 */
public class SubmergedWeaponRule : AttackRule<WeaponAttackContext> {

    override fun evaluate(context: WeaponAttackContext): RuleResult {
        val depth = unitWaterDepth(context.actor.position, context.map)
        if (depth < 2) return RuleResult.Satisfied
        if (context.weapon.underwaterCapable) return RuleResult.Satisfied
        return RuleResult.Unsatisfied(RuleRejection.AttackerSubmerged(depth))
    }
}
