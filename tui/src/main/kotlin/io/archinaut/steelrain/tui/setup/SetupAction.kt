package io.archinaut.steelrain.tui.setup

import io.archinaut.steelrain.tui.input.Action

internal sealed interface SetupAction : Action {
    data class MoveCursor(val delta: Int) : SetupAction

    data class Adjust(val delta: Int) : SetupAction

    data object Toggle : SetupAction

    data object NextPanel : SetupAction

    data object Commit : SetupAction
}
