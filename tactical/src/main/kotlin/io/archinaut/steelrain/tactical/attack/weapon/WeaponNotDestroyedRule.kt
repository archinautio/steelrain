package io.archinaut.steelrain.tactical.attack.weapon

import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.attack.WeaponAttackContext
import io.archinaut.steelrain.tactical.rules.RuleRejection

public class WeaponNotDestroyedRule : AttackRule<WeaponAttackContext> {

    override fun evaluate(context: WeaponAttackContext): RuleResult {
        val weapon = context.weapon
        return if (!weapon.destroyed) {
            RuleResult.Satisfied
        } else {
            RuleResult.Unsatisfied(RuleRejection.WeaponDestroyed(weaponName = weapon.name))
        }
    }
}
