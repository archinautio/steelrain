package io.archinaut.steelrain.tactical.session

import io.archinaut.steelrain.tactical.dice.DiceRoller
import io.archinaut.steelrain.tactical.model.GameMap
import io.archinaut.steelrain.tactical.model.GameState
import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.unit.UnitRoster
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EndPhaseHandlerTest {

    private val emptyState = GameState(
        units = UnitRoster(emptyList()),
        map = GameMap(mapOf(HexCoordinates(0, 0) to io.archinaut.steelrain.tactical.model.Hex(HexCoordinates(0, 0)))),
    )

    @Test
    fun `onEntry emits TurnEnded with the current turn number`() {
        val handler = EndPhaseHandler()
        val turn = TurnState.NULL.copy(turnNumber = 4)

        val outcome = handler.onEntry(emptyState, turn, DiceRoller.seeded(1))

        val turnEnded = outcome.events.filterIsInstance<TurnEnded>().single()
        assertThat(turnEnded.turnNumber).isEqualTo(4)
    }

    @Test
    fun `onEntry advances turnNumber in the outcome turn state`() {
        val handler = EndPhaseHandler()
        val turn = TurnState.NULL.copy(turnNumber = 4)

        val outcome = handler.onEntry(emptyState, turn, DiceRoller.seeded(1))

        assertThat(outcome.turn.turnNumber).isEqualTo(5)
    }
}
