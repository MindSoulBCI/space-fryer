package com.example.spacefryerdiner.p91d27a30.domain.model

data class FoodRecipe(val id: Int, val raw: String, val cooked: String, val seconds: Float, val audioCue: String, val visual: String)
enum class FoodPhase { EMPTY, RAW, COOKING, READY, PLATED, BURNT }
enum class BciConnection { MOCK, CONNECTED, DISCONNECTED, RECONNECTING }
data class GameState(
 val recipes: List<FoodRecipe>, val autoDemo:Boolean=false, val demoElapsed:Float=0f, val selected: Int = 0, val order: Int = 0,
 val phase: FoodPhase = FoodPhase.EMPTY, val progress: Float = 0f,
 val burnAge: Float = 0f, val highFocusSeconds: Float = 0f, val soggy: Boolean = false,
 val focus: Float = .65f, val targetFocus: Float = .65f, val mockWalk: Boolean = true,
 val mood:Float = 1f, val targetMood:Float = 1f,
 val connection: BciConnection = BciConnection.MOCK, val device: String = "离线模拟器",
 val battery: Int = 100, val signal: Int = 4, val remaining: Float = 60f,
 val coins: Int = 0, val combo: Int = 0, val lives: Int = 3, val served: Int = 0,
 val started: Boolean = false, val paused: Boolean = false, val status: String = "按订单取料，再放入炸炉", val grade: String = "—"
) {
 val overdrive get() = highFocusSeconds >= 3f
 val suspended get() = paused || lives <= 0 || connection == BciConnection.DISCONNECTED || connection == BciConnection.RECONNECTING
 val recipe get() = recipes[selected]
 val wanted get() = recipes[order]
}
sealed interface GameEvent {
 data object TakeOver : GameEvent
 data object Take : GameEvent
 data object Drop : GameEvent
 data object Lift : GameEvent
 data object Serve : GameEvent
 data object Discard : GameEvent
 data object Pause : GameEvent
 data object Reset : GameEvent
 data object ToggleConnection : GameEvent
 data object ToggleWalk : GameEvent
 data class Select(val index: Int) : GameEvent
 data class SetFocus(val value: Float) : GameEvent
 data class SetMood(val value:Float):GameEvent
 data class Tick(val seconds: Float) : GameEvent
}
