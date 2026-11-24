package io.archinaut.steelrain.tui

import io.archinaut.steelrain.tactical.model.HexCoordinates
import io.archinaut.steelrain.tactical.model.PlayerId
import io.archinaut.steelrain.tactical.session.GameSession
import io.archinaut.steelrain.tui.game.AppState
import io.archinaut.steelrain.tui.game.mapToTuiPhase
import io.archinaut.steelrain.tui.input.Keybindings
import io.archinaut.steelrain.tui.loop.UiEvent
import io.archinaut.steelrain.tui.loop.runLoop
import io.archinaut.steelrain.tui.input.uiEvents
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import io.archinaut.tenter.terminal.Screen
import io.archinaut.tenter.terminal.TerminalEvent

/**
 * [seats] is the set of seats this process drives, each mapped to the [GameSession] that seat
 * acts and reads through — see [AppState]'s KDoc for the full rationale. The caller composes this
 * map and hands it a session that has already been started (kickstarted, if applicable):
 * hot-seat's shared [io.archinaut.steelrain.tactical.session.BattleSession] has already had
 * [io.archinaut.steelrain.tactical.session.BattleSession.advance] called on it, a `host` seat's server
 * fires its own kickstart once the roster completes, and a `join`ed
 * [io.archinaut.steelrain.network.client.ClientGameSession] never kickstarts at all. This class never builds
 * a session or calls `advance()` itself.
 *
 * [screen] is accepted, not constructed (D17): `Main.kt` builds one tenter terminal for the
 * whole process and hands the [Screen] of its one [io.archinaut.tenter.terminal.Terminal.withScreen] scope
 * to both the setup screen ([io.archinaut.steelrain.tui.setup.SetupApp]) and this class. The scope owns
 * alternate-screen/cursor cleanup; this app's cold input flow acquires and releases raw mode while
 * it is collected, with no flicker at the hand-off between the two screens.
 *
 * [animationsEnabled] turns the weapon-fire overlay played after attacks resolve on or off;
 * by default it is on only when the [ANIMATIONS_ENV_VAR] environment variable is defined.
 */
public class TuiApp(
    private val seats: Map<PlayerId, GameSession>,
    private val screen: Screen,
    private val animationsEnabled: Boolean = System.getenv(ANIMATIONS_ENV_VAR) != null,
) {

    /**
     * Entry point. Wires a subscription for every seat's session into [internalEvents], merges
     * all event sources, and drives [runLoop].
     *
     * ### One subscription per seat, not deduplicated by session identity
     * Hot-seat's two seats share the SAME underlying session object, so subscribing per seat
     * (rather than per distinct session) delivers each event twice there — renders are
     * idempotent, so that's wasteful, not wrong, and keeps this method free of any "is this the
     * same session as another seat?" special-casing. Host/join seat maps have exactly one entry,
     * so they see no duplication at all.
     *
     * ### Single-thread confinement
     * All session mutations, AppState updates, and rendering run on the single
     * [runBlocking] (main) thread. Only the terminal input producer runs on
     * Dispatchers.IO — that is handled internally by [Screen.events].
     *
     * ### Quit ends input collection
     * The keymap's quit chord ends the terminal event flow, which releases raw mode, and is
     * replaced by [UiEvent.Quit] — see [uiEvents] — so [runLoop] stops on its own sentinel.
     */
    public fun run() {
        val keys = Keybindings.DEFAULT

        val appState = AppState(
            seats = seats,
            phase = mapToTuiPhase(seats.values.first().currentPhase),
            cursor = HexCoordinates(0, 0),
        )

        runBlocking {
            val internalEvents = Channel<UiEvent>(Channel.UNLIMITED)
            val subscriptions = seats.values.map { session ->
                session.subscribe { internalEvents.trySend(UiEvent.Session(it)) }
            }
            try {
                runLoop(
                    events = merge(
                        screen.uiEvents(keys, quit = UiEvent.Quit) { it.toUiEvent() },
                        internalEvents.receiveAsFlow(),
                    ),
                    internalEvents = internalEvents,
                    screen = screen,
                    initialState = appState,
                    keys = keys,
                    animationsEnabled = animationsEnabled,
                )
            } finally {
                subscriptions.forEach { it.unsubscribe() }
            }
        }
    }
}

/** Defining this environment variable (to any value) turns on the weapon-fire animations. */
internal const val ANIMATIONS_ENV_VAR: String = "STEELRAIN_ANIMATIONS"

/** tenter's generic [TerminalEvent] mapped onto this app's own [UiEvent] hierarchy — see [UiEvent]'s KDoc for why it has a [UiEvent.Session] arm tenter cannot know about. */
private fun TerminalEvent.toUiEvent(): UiEvent = when (this) {
    is TerminalEvent.Input -> UiEvent.Input(event)
    is TerminalEvent.Resized -> UiEvent.Resized(size)
}
