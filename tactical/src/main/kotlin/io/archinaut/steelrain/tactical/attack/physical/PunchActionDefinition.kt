package io.archinaut.steelrain.tactical.attack.physical

import io.archinaut.steelrain.tactical.attack.AttackDefinition
import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.attack.PhysicalAttackContext
import io.archinaut.steelrain.tactical.attack.weapon.HeatPenaltyRule

public class PunchActionDefinition : AttackDefinition<PhysicalAttackContext> {

    override val name: String = "Punch"

    override val rules: List<AttackRule<PhysicalAttackContext>> = listOf(
        TargetAliveRule(),
        AdjacentRule(),
        PunchReachRule(),
        PunchMovementRule(),
        ProneAttackerRule(),
        HeatPenaltyRule(),
    )
}
