package io.archinaut.steelrain.tactical.attack.weapon

import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.attack.WeaponAttackContext
import io.archinaut.steelrain.tactical.rules.RuleRejection
import io.archinaut.steelrain.tactical.unit.availableAmmoBins

public class HasAmmoRule : AttackRule<WeaponAttackContext> {

    override fun evaluate(context: WeaponAttackContext): RuleResult {
        val weapon = context.weapon
        val type = weapon.ammoType ?: return RuleResult.Satisfied
        val remaining = context.actor.availableAmmoBins()
            .filter { it.third.type == type }.sumOf { it.third.shots }
        return if (remaining > 0) {
            RuleResult.Satisfied
        } else {
            RuleResult.Unsatisfied(RuleRejection.NoAmmo(weaponName = weapon.name))
        }
    }
}
