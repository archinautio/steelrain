package io.archinaut.steelrain.tui.input

import io.archinaut.steelrain.tactical.model.HexDirection

public sealed interface FacingAction : Action {
    public data class SelectFacing(val direction: HexDirection) : FacingAction

    public data object Cancel : FacingAction

    public data object CycleUnit : FacingAction
}
