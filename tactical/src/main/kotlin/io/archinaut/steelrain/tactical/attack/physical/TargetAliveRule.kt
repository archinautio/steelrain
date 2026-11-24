package io.archinaut.steelrain.tactical.attack.physical

import io.archinaut.steelrain.tactical.attack.AttackContext
import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.rules.RuleRejection

public class TargetAliveRule : AttackRule<AttackContext> {

    override fun evaluate(context: AttackContext): RuleResult =
        if (!context.target.isDestroyed) {
            RuleResult.Satisfied
        } else {
            RuleResult.Unsatisfied(RuleRejection.TargetDestroyed)
        }
}
