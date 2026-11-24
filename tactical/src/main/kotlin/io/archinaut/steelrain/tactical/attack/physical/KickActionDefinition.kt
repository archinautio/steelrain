package io.archinaut.steelrain.tactical.attack.physical

import io.archinaut.steelrain.tactical.attack.AttackDefinition
import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.attack.PhysicalAttackContext
import io.archinaut.steelrain.tactical.attack.weapon.HeatPenaltyRule

public class KickActionDefinition : AttackDefinition<PhysicalAttackContext> {

    override val name: String = "Kick"

    override val rules: List<AttackRule<PhysicalAttackContext>> = listOf(
        TargetAliveRule(),
        AdjacentRule(),
        KickReachRule(),
        KickMovementRule(),
        ProneAttackerRule(),
        HeatPenaltyRule(),
    )
}
