package io.archinaut.steelrain.tui.view

import io.archinaut.tenter.widget.MeasurableWidget
import io.archinaut.steelrain.tactical.unit.ForeignUnit
import io.archinaut.tenter.widget.RowWriter

internal class TargetStatusView(private val unit: ForeignUnit) : MeasurableWidget {

    override fun write(writer: RowWriter) {
        ForeignUnitPanel.render(writer, unit)
    }

    internal companion object {
        internal const val TITLE: String = "TARGET STATUS"
    }
}
