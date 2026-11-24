package io.archinaut.steelrain.tui

import io.archinaut.steelrain.tactical.model.GameMap
import io.archinaut.steelrain.tactical.model.GameState
import io.archinaut.steelrain.tactical.model.Hex
import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.TurnPhase
import io.archinaut.steelrain.tactical.session.HeatDissipated
import io.archinaut.steelrain.tactical.session.PhaseChanged
import io.archinaut.steelrain.tactical.session.SessionNotice
import io.archinaut.steelrain.tactical.unit.UnitRoster
import io.archinaut.steelrain.tui.icon.heatChangeIcon
import io.archinaut.steelrain.tui.icon.sessionNoticeIcon
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GameEventPrinterTest {

    private val emptyState = GameState(
        units = UnitRoster(emptyList()),
        map = GameMap(mapOf(HexCoordinates(0, 0) to Hex(HexCoordinates(0, 0)))),
    )

    private val heatDissipatedEvent = HeatDissipated(heatBefore = emptyMap(), heatAfter = emptyMap())

    @Test
    fun `prints a turn header only when the turn number changes`() {
        val out = StringBuilder()
        val printer = GameEventPrinter(out)

        printer.print(heatDissipatedEvent, emptyState, turnNumber = 1)
        printer.print(heatDissipatedEvent, emptyState, turnNumber = 1)
        printer.print(heatDissipatedEvent, emptyState, turnNumber = 2)

        val headerCount = out.lines().count { it == "== TURN 1 ==" }
        assertThat(headerCount).isEqualTo(1)
        assertThat(out.toString()).contains("== TURN 2 ==")
        assertThat(out.lines().count { it == "== TURN 2 ==" }).isEqualTo(1)
    }

    @Test
    fun `HeatDissipated with no heat to dissipate renders with the heat-down icon`() {
        val out = StringBuilder()
        val printer = GameEventPrinter(out)

        printer.print(heatDissipatedEvent, emptyState, turnNumber = 1)

        assertThat(out.toString()).contains("${heatChangeIcon(wentUp = false)} Heat: no heat to dissipate")
    }

    @Test
    fun `SessionNotice renders with the lan-connect icon`() {
        val out = StringBuilder()
        val printer = GameEventPrinter(out)

        printer.print(SessionNotice("Session ID: ABC123"), emptyState, turnNumber = 1)

        assertThat(out.toString()).contains("${sessionNoticeIcon()} Session ID: ABC123")
    }

    @Test
    fun `PhaseChanged prints nothing, including no turn header`() {
        val out = StringBuilder()
        val printer = GameEventPrinter(out)

        printer.print(PhaseChanged(TurnPhase.INITIATIVE, TurnPhase.MOVEMENT), emptyState, turnNumber = 1)

        assertThat(out.toString()).isEmpty()
    }
}
