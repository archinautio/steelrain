package io.archinaut.steelrain.tactical.attack

import io.archinaut.steelrain.tactical.rules.RuleResult

public interface AttackRule<in C : AttackContext> {
    public fun evaluate(context: C): RuleResult
}
