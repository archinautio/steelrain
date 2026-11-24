package io.archinaut.steelrain.tactical.attack.physical

import io.archinaut.steelrain.tactical.attack.PhysicalAttackContext
import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.query.aGameState
import io.archinaut.steelrain.tactical.query.aUnit
import io.archinaut.steelrain.tactical.rules.RuleRejection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ProneAttackerRuleTest {

    private fun context(prone: Boolean): PhysicalAttackContext {
        val actor = aUnit(id = "actor", position = HexCoordinates(0, 0)).copy(isProne = prone)
        val target = aUnit(id = "target", position = HexCoordinates(1, 0))
        return PhysicalAttackContext(actor, aGameState(units = listOf(actor, target)).map, target)
    }

    @Test
    fun `a prone attacker cannot make a physical attack`() {
        val result = ProneAttackerRule().evaluate(context(prone = true))
        assertThat(result).isInstanceOf(RuleResult.Unsatisfied::class.java)
        assertThat((result as RuleResult.Unsatisfied).reason)
            .isInstanceOf(RuleRejection.AttackerProne::class.java)
    }

    @Test
    fun `a standing attacker may make a physical attack`() {
        assertThat(ProneAttackerRule().evaluate(context(prone = false))).isEqualTo(RuleResult.Satisfied)
    }
}
