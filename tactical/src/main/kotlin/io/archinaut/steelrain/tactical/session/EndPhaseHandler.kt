package io.archinaut.steelrain.tactical.session

import io.archinaut.steelrain.tactical.attack.resetTorsoFacings
import io.archinaut.steelrain.tactical.dice.DiceRoller
import io.archinaut.steelrain.tactical.model.GameState
import io.archinaut.steelrain.tactical.model.TurnPhase

/**
 * System phase. On entry, resets torso facings (units' torsos snap back to
 * leg facing) and emits a turn-end event. The cascade then rolls over to
 * [InitiativePhaseHandler], which rebuilds a fresh [TurnState] for the
 * next turn.
 */
public class EndPhaseHandler : SystemPhaseHandler() {

    override val phase: TurnPhase = TurnPhase.END

    override fun onEntry(
        state: GameState,
        turn: TurnState,
        roller: DiceRoller,
    ): PhaseOutcome {
        val resetState = state.resetTorsoFacings()
        return PhaseOutcome(
            state = resetState,
            turn = turn.copy(turnNumber = turn.turnNumber + 1),
            events = listOf(TurnEnded(turnNumber = turn.turnNumber)),
        )
    }
}
