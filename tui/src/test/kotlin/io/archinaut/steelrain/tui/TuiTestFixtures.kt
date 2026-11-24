package io.archinaut.steelrain.tui

import io.archinaut.steelrain.tactical.dice.DiceRoll
import io.archinaut.steelrain.tactical.model.GameMap
import io.archinaut.steelrain.tactical.model.GameState
import io.archinaut.steelrain.tactical.model.Hex
import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.HexDirection
import io.archinaut.steelrain.tactical.model.MechLocation
import io.archinaut.steelrain.tactical.model.PlayerId
import io.archinaut.steelrain.tactical.model.Terrain
import io.archinaut.steelrain.tactical.query.PlayerView
import io.archinaut.steelrain.tactical.session.AttackProgress
import io.archinaut.steelrain.tactical.session.Impulse
import io.archinaut.steelrain.tactical.session.ImpulseSequence
import io.archinaut.steelrain.tactical.session.Initiative
import io.archinaut.steelrain.tactical.session.MovementProgress
import io.archinaut.steelrain.tactical.session.TurnState
import io.archinaut.steelrain.tactical.unit.ArmorLayout
import io.archinaut.steelrain.tactical.unit.CombatUnit
import io.archinaut.steelrain.tactical.unit.CriticalLayout
import io.archinaut.steelrain.tactical.unit.HeatSink
import io.archinaut.steelrain.tactical.unit.HeatSinkType
import io.archinaut.steelrain.tactical.unit.InternalStructureLayout
import io.archinaut.steelrain.tactical.unit.MechModel
import io.archinaut.steelrain.tactical.unit.UnitId
import io.archinaut.steelrain.tactical.unit.UnitRoster
import io.archinaut.steelrain.tactical.unit.Weapon
import io.archinaut.steelrain.tactical.unit.WeaponModels
import io.archinaut.steelrain.tactical.unit.WeaponMountId
import io.archinaut.steelrain.tactical.unit.empty
import io.archinaut.steelrain.tui.game.AppState
import io.archinaut.steelrain.tui.game.phase.MovementPhase
import io.archinaut.steelrain.tui.game.phase.Phase
import io.archinaut.tenter.terminal.HeadlessTerminal

internal fun mediumLaser(
    mountId: WeaponMountId = WeaponMountId(0),
    location: MechLocation = MechLocation.CENTER_TORSO,
): Weapon = Weapon(model = WeaponModels.mediumLaser, mountId = mountId, location = location)

internal fun srm6(
    mountId: WeaponMountId = WeaponMountId(1),
    location: MechLocation = MechLocation.LEFT_TORSO,
): Weapon = Weapon(model = WeaponModels.srm6, mountId = mountId, location = location)

internal fun aTurnState(
    movementOrder: List<Impulse> = listOf(Impulse(PlayerId.PLAYER_1, 1)),
    currentImpulseIndex: Int = 0,
    movedUnitIds: Set<UnitId> = emptySet(),
    attackOrder: List<Impulse> = listOf(Impulse(PlayerId.PLAYER_1, 1), Impulse(PlayerId.PLAYER_2, 1)),
    currentAttackImpulseIndex: Int = 0,
): TurnState = TurnState(
    initiative = Initiative(
        rolls = mapOf(PlayerId.PLAYER_1 to DiceRoll(2, 3), PlayerId.PLAYER_2 to DiceRoll(4, 4)),
        loser = PlayerId.PLAYER_1,
        winner = PlayerId.PLAYER_2,
    ),
    movement = MovementProgress(
        sequence = ImpulseSequence(movementOrder, currentImpulseIndex),
        movedUnitIds = movedUnitIds,
    ),
    attack = AttackProgress(
        sequence = ImpulseSequence(attackOrder, currentAttackImpulseIndex),
    ),
)

internal fun anAppState(
    phase: Phase,
    cursor: HexCoordinates = HexCoordinates(0, 0),
    gameState: GameState = aGameState(),
    turnState: TurnState = aTurnState(),
): AppState = AppState(gameState, turnState, phase, cursor)

/**
 * A [PlayerView] for [player] over [gameState], routed through a throwaway [AppState]/session
 * instead of constructing `DefaultPlayerView` directly — keeps test code on the same
 * `session.viewFor` path production code uses.
 *
 * [AppState.view] is scoped to [AppState.viewer] and takes no player argument, so [player] is
 * selected by seating them alone — exactly how a host/join client is composed.
 */
internal fun viewFor(player: PlayerId, gameState: GameState): PlayerView =
    anAppState(MovementPhase.SelectingUnit, gameState = gameState)
        .let { it.copy(seats = mapOf(player to it.anySession)) }
        .view

internal fun aGameMap(
    cols: Int = 3,
    rows: Int = 3,
    terrain: Terrain = Terrain.CLEAR,
): GameMap {
    val hexes = mutableMapOf<HexCoordinates, Hex>()
    for (col in 0 until cols) {
        for (row in 0 until rows) {
            val coords = HexCoordinates(col, row)
            hexes[coords] = Hex(coords, terrain)
        }
    }
    return GameMap(hexes)
}

internal fun anInternalStructureLayout(
    head: Int = 3,
    centerTorso: Int = 31,
    leftTorso: Int = 21,
    rightTorso: Int = 21,
    leftArm: Int = 17,
    rightArm: Int = 17,
    leftLeg: Int = 21,
    rightLeg: Int = 21,
): InternalStructureLayout = InternalStructureLayout(
    head = head,
    centerTorso = centerTorso,
    leftTorso = leftTorso,
    rightTorso = rightTorso,
    leftArm = leftArm,
    rightArm = rightArm,
    leftLeg = leftLeg,
    rightLeg = rightLeg,
)

internal fun aUnit(
    id: String = "unit-1",
    owner: PlayerId = PlayerId.PLAYER_1,
    name: String = "Atlas",
    position: HexCoordinates = HexCoordinates(0, 0),
    facing: HexDirection = HexDirection.N,
    walkingMP: Int = 0,
    runningMP: Int = 0,
    jumpMP: Int = 0,
    currentHeat: Int = 0,
    weapons: List<Weapon> = emptyList(),
    armor: ArmorLayout = anArmorLayout(),
    maxArmor: ArmorLayout = armor,
    heatSink: HeatSink = HeatSink(HeatSinkType.STS, 10),
    internalStructure: InternalStructureLayout = anInternalStructureLayout(),
    maxInternalStructure: InternalStructureLayout = internalStructure,
    criticalLayout: CriticalLayout = CriticalLayout.empty(),
): CombatUnit = CombatUnit(
    model = MechModel(
        variant = "TEST",
        name = name,
        tonnage = 50,
        walkingMP = walkingMP,
        runningMP = runningMP,
        jumpMP = jumpMP,
        heatSink = heatSink,
        armor = maxArmor,
        internalStructure = maxInternalStructure,
        criticalLayout = criticalLayout,
        weapons = weapons,
    ),
    id = UnitId(id),
    owner = owner,
    gunnerySkill = 4,
    pilotingSkill = 5,
    weapons = weapons,
    position = position,
    facing = facing,
    currentHeat = currentHeat,
    armor = armor,
    internalStructure = internalStructure,
    criticalLayout = criticalLayout,
)

internal fun anArmorLayout(
    head: Int = 9,
    centerTorso: Int = 47, centerTorsoRear: Int = 14,
    leftTorso: Int = 32, leftTorsoRear: Int = 10,
    rightTorso: Int = 32, rightTorsoRear: Int = 10,
    leftArm: Int = 34, rightArm: Int = 34,
    leftLeg: Int = 41, rightLeg: Int = 41,
): ArmorLayout = ArmorLayout(
    head = head,
    centerTorso = centerTorso, centerTorsoRear = centerTorsoRear,
    leftTorso = leftTorso, leftTorsoRear = leftTorsoRear,
    rightTorso = rightTorso, rightTorsoRear = rightTorsoRear,
    leftArm = leftArm, rightArm = rightArm,
    leftLeg = leftLeg, rightLeg = rightLeg,
)

internal fun aGameState(
    units: List<CombatUnit> = emptyList(),
    map: GameMap = aGameMap(),
): GameState = GameState(
    units = UnitRoster(units),
    map = map,
)

/** The text of what this terminal shows, one line per row. */
internal fun HeadlessTerminal.frameText(): String = frame.text()
