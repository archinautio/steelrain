package io.archinaut.steelrain.tui.game

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.PlayerId
import io.archinaut.steelrain.tactical.session.Impulse
import io.archinaut.steelrain.tactical.session.TurnState
import io.archinaut.steelrain.tactical.unit.UnitId
import io.archinaut.steelrain.tui.aGameState
import io.archinaut.steelrain.tui.aTurnState
import io.archinaut.steelrain.tui.aUnit
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UnitSelectionTest {

    private fun aMovementTurn(
        activePlayer: PlayerId = PlayerId.PLAYER_1,
        movedUnitIds: Set<UnitId> = emptySet(),
    ): TurnState = aTurnState(
        movementOrder = listOf(Impulse(activePlayer, 2)),
        movedUnitIds = movedUnitIds,
    )

    @Test
    fun `returns active player unmoved units`() {
        val u1 = aUnit(id = "u1", owner = PlayerId.PLAYER_1, position = HexCoordinates(0, 0))
        val u2 = aUnit(id = "u2", owner = PlayerId.PLAYER_1, position = HexCoordinates(1, 0))
        val u3 = aUnit(id = "u3", owner = PlayerId.PLAYER_2, position = HexCoordinates(2, 0))
        val gameState = aGameState(units = listOf(u1, u2, u3))
        val turnState = aMovementTurn(activePlayer = PlayerId.PLAYER_1)

        val result = turnState.selectableUnits(gameState.units)

        assertEquals(listOf(u1, u2), result)
    }

    @Test
    fun `excludes already moved units`() {
        val u1 = aUnit(id = "u1", owner = PlayerId.PLAYER_1, position = HexCoordinates(0, 0))
        val u2 = aUnit(id = "u2", owner = PlayerId.PLAYER_1, position = HexCoordinates(1, 0))
        val gameState = aGameState(units = listOf(u1, u2))
        val turnState = aMovementTurn(
            activePlayer = PlayerId.PLAYER_1,
            movedUnitIds = setOf(UnitId("u1")),
        )

        val result = turnState.selectableUnits(gameState.units)

        assertEquals(listOf(u2), result)
    }

    @Test
    fun `returns empty when no selectable units`() {
        val u1 = aUnit(id = "u1", owner = PlayerId.PLAYER_2, position = HexCoordinates(0, 0))
        val gameState = aGameState(units = listOf(u1))
        val turnState = aMovementTurn(activePlayer = PlayerId.PLAYER_1)

        val result = turnState.selectableUnits(gameState.units)

        assertTrue(result.isEmpty())
    }
}
