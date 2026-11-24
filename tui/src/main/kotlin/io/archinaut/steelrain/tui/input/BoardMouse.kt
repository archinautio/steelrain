package io.archinaut.steelrain.tui.input

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tui.hex.HexLayout
import com.github.ajalt.mordant.input.MouseEvent
import io.archinaut.tenter.screen.Point

internal object BoardMouse {
    fun mapContentToHex(event: MouseEvent, contentPoint: Point?): HexCoordinates? {
        if (!event.left) return null
        val point = contentPoint ?: return null
        val x = point.x - io.archinaut.steelrain.tui.view.BoardView.MAP_ORIGIN_X
        val y = point.y - io.archinaut.steelrain.tui.view.BoardView.MAP_ORIGIN_Y
        if (x < 0 || y < 0) return null
        return HexLayout.screenToHex(x, y, scrollX = 0, scrollY = 0)
    }
}
