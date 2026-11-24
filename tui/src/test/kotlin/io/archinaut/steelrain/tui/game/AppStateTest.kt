package io.archinaut.steelrain.tui.game

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.HexDirection
import io.archinaut.steelrain.tactical.model.TurnPhase
import io.archinaut.steelrain.tactical.session.TurnState
import io.archinaut.steelrain.tui.aGameMap
import io.archinaut.steelrain.tui.game.phase.MovementPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AppStateTest {

    @Nested
    inner class CurrentPhaseTest {
        @Test
        fun `currentPhase reflects movement when in MovementPhase`() {
            val app = AppState(
                io.archinaut.steelrain.tui.aGameState(),
                TurnState.NULL,
                MovementPhase.SelectingUnit,
                HexCoordinates(0, 0),
            )
            assertEquals(TurnPhase.MOVEMENT, app.currentPhase)
        }
    }

    @Nested
    inner class MoveCursorTest {
        @Test
        fun `moves to neighbor within bounds`() {
            val map = aGameMap(cols = 5, rows = 5)
            val result = moveCursor(HexCoordinates(2, 2), HexDirection.N, map)
            assertEquals(HexCoordinates(2, 1), result)
        }

        @Test
        fun `stays in place when out of bounds`() {
            val map = aGameMap()
            val result = moveCursor(HexCoordinates(0, 0), HexDirection.N, map)
            assertEquals(HexCoordinates(0, 0), result)
        }
    }
}
