package com.example.spacefryerdiner.p91d27a30.ui.game
import org.junit.Test
import org.junit.Assert.*
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import com.example.spacefryerdiner.p91d27a30.data.repository.LocalFoodRepository
class GameViewModelTest {
 private fun vm()=GameViewModel(LocalFoodRepository().all()).also { it.onEntry(EntryEvent.Guest);it.onEvent(GameEvent.TakeOver) }
 @Test fun catalogHasFortyUniqueRecipes(){val s=vm().state.value;assertEquals(40,s.recipes.size);assertEquals(40,s.recipes.map{it.id}.toSet().size)}
 @Test fun takeAndDrop(){val v=vm();v.onEvent(GameEvent.Take);v.onEvent(GameEvent.Drop);assertEquals(FoodPhase.COOKING,v.state.value.phase)}
 @Test fun focusClampsAndStopsRandomWalk(){val v=vm();v.onEvent(GameEvent.SetFocus(2f));assertEquals(1f,v.state.value.targetFocus,0f);v.walk(-.5f);assertEquals(1f,v.state.value.targetFocus,0f)}
 @Test fun selectionCannotChangeHeldFood(){val v=vm();v.onEvent(GameEvent.Take);v.onEvent(GameEvent.Select(3));assertEquals(0,v.state.value.selected)}
 @Test fun menuDoesNotConsumeOrder(){val v=vm();v.onEvent(GameEvent.Tick(90f));assertEquals(60f,v.state.value.remaining,0f);assertEquals(3,v.state.value.lives)}
 @Test fun restartClearsRound(){val v=vm();v.onEvent(GameEvent.Take);v.onEvent(GameEvent.Reset);assertEquals(FoodPhase.EMPTY,v.state.value.phase);assertEquals(3,v.state.value.lives)}
}
