package io.archinaut.steelrain.tactical.attack.weapon

import io.archinaut.steelrain.tactical.attack.AttackContext
import io.archinaut.steelrain.tactical.attack.AttackRule
import io.archinaut.steelrain.tactical.attack.lineOfSight
import io.archinaut.steelrain.tactical.model.Terrain
import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.rules.RuleRejection

/** Blocks an attack when line of sight is obstructed (`docs/site/rules/line-of-sight.md` §1–2). */
public class LineOfSightRule : AttackRule<AttackContext> {

    override fun evaluate(context: AttackContext): RuleResult {
        val los = lineOfSight(context.actor.position, context.target.position, context.map)
        return if (los.blocked) {
            RuleResult.Unsatisfied(
                RuleRejection.NoLineOfSight(
                    blockerAt = los.blockerHex ?: context.target.position,
                    blockingTerrain = los.blockingTerrain ?: Terrain.CLEAR,
                ),
            )
        } else {
            RuleResult.Satisfied
        }
    }
}
