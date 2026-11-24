package io.archinaut.steelrain.tactical.attack.weapon

import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.rules.Warning
import io.archinaut.steelrain.tactical.attack.AttackContext
import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.heat.HeatScale

public class HeatPenaltyRule : AttackRule<AttackContext> {

    override fun evaluate(context: AttackContext): RuleResult {
        val actor = context.actor
        val modifier = HeatScale.toHitPenalty(actor.currentHeat)
        return if (modifier == 0) {
            RuleResult.Satisfied
        } else {
            RuleResult.Penalized(
                Warning(
                    code = "HEAT_PENALTY",
                    description = "Heat ${actor.currentHeat}, +$modifier to-hit modifier",
                    modifier = modifier,
                )
            )
        }
    }
}
