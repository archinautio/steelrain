package io.archinaut.steelrain.tactical.attack.physical

import io.archinaut.steelrain.tactical.attack.aPhysicalAttackContext
import io.archinaut.steelrain.tactical.rules.RuleResult
import io.archinaut.steelrain.tactical.query.aUnit
import io.archinaut.steelrain.tactical.rules.RuleRejection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TargetAliveRuleTest {

    private val rule = TargetAliveRule()

    @Test
    fun `satisfied when target is alive`() {
        val target = aUnit(id = "target")
        val result = rule.evaluate(aPhysicalAttackContext(target = target))
        assertThat(result).isEqualTo(RuleResult.Satisfied)
    }

    @Test
    fun `unsatisfied with TargetDestroyed when target is destroyed`() {
        val target = aUnit(id = "target", isDestroyed = true)
        val result = rule.evaluate(aPhysicalAttackContext(target = target))
        assertThat(result).isInstanceOf(RuleResult.Unsatisfied::class.java)
        val unsatisfied = result as RuleResult.Unsatisfied
        assertThat(unsatisfied.reason).isEqualTo(RuleRejection.TargetDestroyed)
    }
}
