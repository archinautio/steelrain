package io.archinaut.steelrain

import io.archinaut.steelrain.strategic.StrategicRules

public fun main() {
    println("Steelrain Rules Engine")
    println("======================")

    val strategic = StrategicRules()

    println("Strategic: Campaign movement for 5 hexes = ${strategic.calculateCampaignMovement(5)}")
}
