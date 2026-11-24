package io.archinaut.steelrain.tui.game.phase

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.HexDirection
import io.archinaut.steelrain.tactical.model.MovementMode
import io.archinaut.steelrain.tactical.model.PlayerId
import io.archinaut.steelrain.tactical.model.TurnPhase
import io.archinaut.steelrain.tactical.movement.ReachabilityMap
import io.archinaut.steelrain.tactical.session.Impulse
import io.archinaut.steelrain.tui.aGameState
import io.archinaut.steelrain.tui.aTurnState
import io.archinaut.steelrain.tui.aUnit
import io.archinaut.steelrain.tui.anAppState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class PhaseStatusTest {

    private val unit = aUnit(id = "W1", name = "Wolverine WVR-6R")
    private val reachability = ReachabilityMap(MovementMode.WALK, maxMP = 5, destinations = emptyList())

    @Test
    fun `selected action phases expose their unit`() {
        val phases = listOf(
            MovementPhase.Browsing(
                unitId = unit.id,
                modes = listOf(reachability),
                currentModeIndex = 0,
                hoveredDestination = null,
            ),
            MovementPhase.SelectingFacing(
                unitId = unit.id,
                modes = listOf(reachability),
                currentModeIndex = 0,
                hex = HexCoordinates(0, 0),
                options = emptyList(),
            ),
            AttackPhase.Declaring(
                attackTurnPhase = TurnPhase.WEAPON_ATTACK,
                unitId = unit.id,
                allocation = WeaponAllocation(torsoFacing = HexDirection.N),
            ),
            PhysicalAttackPhase.Declaring(
                unitId = unit.id,
                cursorIndex = 0,
                assignments = emptyMap(),
            ),
        )

        for (phase in phases) {
            val app = anAppState(phase, gameState = aGameState(units = listOf(unit)))

            assertEquals(unit.id, phase.status(app).actionUnitId)
        }
    }

    @Test
    fun `unit selection phases expose no action unit`() {
        val phases = listOf(
            MovementPhase.SelectingUnit,
            AttackPhase.SelectingAttacker(TurnPhase.WEAPON_ATTACK),
            PhysicalAttackPhase.SelectingAttacker(),
        )

        for (phase in phases) {
            val app = anAppState(phase, gameState = aGameState(units = listOf(unit)))

            assertNull(phase.status(app).actionUnitId)
        }
    }

    @Test
    fun `movement selection clarifies remaining count is for current impulse`() {
        val initialTurnState = aTurnState(
            movementOrder = listOf(Impulse(PlayerId.PLAYER_1, 3)),
        )
        val turnState = initialTurnState.copy(
            movement = initialTurnState.movement.afterUnitMoved(unit.id),
        )
        val app = anAppState(
            phase = MovementPhase.SelectingUnit,
            gameState = aGameState(units = listOf(unit)),
            turnState = turnState,
        )

        val status = MovementPhase.SelectingUnit.status(app)

        assertEquals(
            "Select a unit to move (2 remaining in this impulse)",
            status.prompt,
        )
    }
}
