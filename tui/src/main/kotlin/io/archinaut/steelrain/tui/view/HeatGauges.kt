package io.archinaut.steelrain.tui.view

import io.archinaut.steelrain.tui.screen.AppRole
import io.archinaut.tenter.widget.MeasurableWidget
import io.archinaut.steelrain.tactical.heat.HeatScale
import io.archinaut.steelrain.tactical.heat.projectHeat
import io.archinaut.steelrain.tactical.model.GameMap
import io.archinaut.steelrain.tactical.unit.CombatUnit
import io.archinaut.steelrain.tactical.unit.HeatSource
import io.archinaut.tenter.screen.Style
import io.archinaut.tenter.widget.RowWriter

/**
 * The "Current" gauge, this-turn heat sources (committed solid, [pendingHeat] drafted), the sink
 * dissipation gauge, and the "Projected" gauge — the [io.archinaut.steelrain.tactical.heat.HeatProjection]
 * preview shared by the NORMAL [UnitStatusView] and the maximized record sheet's
 * [io.archinaut.steelrain.tui.view.record.HeatLadder]. Neither card's surrounding header/spacing is drawn
 * here — callers wrap this with their own [SectionHeading] and blank lines.
 */
internal class HeatGauges(
    private val unit: CombatUnit,
    private val map: GameMap,
    private val pendingHeat: List<HeatSource>,
) : MeasurableWidget {

    override fun write(writer: RowWriter) {
        val projection = projectHeat(unit, map, pendingHeat)
        writer.writeLine("Current")
        writer.draw(heatBar(unit.currentHeat))
        for (source in projection.committed) writer.writeLine("  ${source.label} +${source.amount}")
        for (source in projection.pending) writer.writeLine("  ${source.label} +${source.amount}", DRAFT_STYLE)

        val sink = unit.heatSink
        val sinkSuffix =
            if (sink.type.sinkRatio == 1) "${sink.type.name} ${projection.dissipation}"
            else "${sink.type.name} ${sink.units}(${projection.dissipation})"
        writer.draw(BarGauge(barWidth = 10, maxValue = projection.dissipation, value = projection.dissipated, suffix = sinkSuffix))

        writer.writeLine("Projected")
        writer.draw(heatBar(projection.projected))
    }

    private fun heatBar(heat: Int): BarGauge = BarGauge(barWidth = 20, maxValue = HeatScale.MAX_HEAT, value = heat)

    private companion object {
        private val DRAFT_STYLE = Style(AppRole.DRAFT)
    }
}
