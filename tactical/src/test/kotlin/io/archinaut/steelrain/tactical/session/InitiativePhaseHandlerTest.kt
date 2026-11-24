package io.archinaut.steelrain.tactical.session

import io.archinaut.steelrain.tactical.dice.DiceRoller
import io.archinaut.steelrain.tactical.model.GameMap
import io.archinaut.steelrain.tactical.model.GameState
import io.archinaut.steelrain.tactical.model.Hex
import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.unit.UnitRoster
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InitiativePhaseHandlerTest {

    private val emptyState = GameState(
        units = UnitRoster(emptyList()),
        map = GameMap(mapOf(HexCoordinates(0, 0) to Hex(HexCoordinates(0, 0)))),
    )

    @Test
    fun `onEntry preserves the incoming turnNumber on the rebuilt TurnState`() {
        val handler = InitiativePhaseHandler()
        val incoming = TurnState.NULL.copy(turnNumber = 5)

        val outcome = handler.onEntry(emptyState, incoming, DiceRoller.seeded(1))

        assertThat(outcome.turn.turnNumber).isEqualTo(5)
    }
}
