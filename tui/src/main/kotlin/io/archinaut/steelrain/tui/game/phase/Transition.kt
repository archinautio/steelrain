package io.archinaut.steelrain.tui.game.phase

import io.archinaut.steelrain.tui.game.AppState
import io.archinaut.tenter.widget.FlashMessage

internal data class Transition(
    val app: AppState,
    val flash: FlashMessage? = null,
)
