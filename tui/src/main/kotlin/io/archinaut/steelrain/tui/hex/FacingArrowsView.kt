package io.archinaut.steelrain.tui.hex

import io.archinaut.steelrain.tactical.model.HexDirection
import io.archinaut.steelrain.tactical.model.MovementMode
import io.archinaut.steelrain.tui.icon.facingIcon
import io.archinaut.steelrain.tui.icon.movementModeIcon
import io.archinaut.steelrain.tui.screen.BoardRole
import io.archinaut.tenter.screen.Canvas
import io.archinaut.tenter.screen.Style
import io.archinaut.tenter.widget.Widget

/**
 * The reachable [facings] at one hex, in [color]. When every facing is reachable, one center mark
 * (the [movementMode] icon, or a dot) stands for all of them; otherwise each gets its own arrow.
 */
internal class FacingArrowsView(
    private val facings: Set<HexDirection>,
    private val color: BoardRole,
    private val movementMode: MovementMode? = null,
) : Widget {

    override fun render(canvas: Canvas) {
        val style = Style(fg = color)
        if (facings.size == HexDirection.entries.size) {
            val icon = if (movementMode != null) movementModeIcon(movementMode) else "."
            canvas.setString(HexCells.OVERLAY_X, HexCells.OVERLAY_Y, icon, style)
            return
        }
        for (direction in facings) {
            val (x, y) = HexCells.facing(direction)
            canvas.setString(x, y, facingIcon(direction), style)
        }
    }
}
