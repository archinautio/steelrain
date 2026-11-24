package io.archinaut.steelrain.tui.view

import io.archinaut.steelrain.tactical.dice.DiceRoll
import io.archinaut.steelrain.tui.icon.diceIcon

internal fun diceRollLabel(roll: DiceRoll): String =
    "${diceIcon(roll.d1)}+${diceIcon(roll.d2)}=${roll.total}"
