package com.example.spacefryerdiner.p91d27a30.ui.game
import org.junit.Test
import org.junit.Assert.*
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import com.example.spacefryerdiner.p91d27a30.data.repository.LocalFoodRepository
class GuestSessionTest {
 private fun vm()=GameViewModel(LocalFoodRepository().all())
 @Test fun entryBlocksPlayAndClock(){val v=vm();v.onEvent(GameEvent.Take);v.onEvent(GameEvent.Tick(100f));assertFalse(v.entry.value.entered);assertFalse(v.state.value.started);assertEquals(FoodPhase.EMPTY,v.state.value.phase)}
 @Test fun demoServesMultipleRandomFoods(){val v=vm();v.onEntry(EntryEvent.Guest);val chosen=mutableSetOf<Int>();repeat(4000){v.onEvent(GameEvent.Tick(.05f));chosen.add(v.state.value.selected)};assertTrue(v.state.value.served>=8);assertTrue(chosen.size>1);assertEquals(3,v.state.value.lives)}
 @Test fun takeoverPreservesCookingAndStopsFutureActions(){val v=vm();v.onEntry(EntryEvent.Guest);repeat(60){v.onEvent(GameEvent.Tick(.05f))};val before=v.state.value;assertEquals(FoodPhase.COOKING,before.phase);v.onEvent(GameEvent.TakeOver);val after=v.state.value;assertFalse(after.autoDemo);assertEquals(before.progress,after.progress,0f);assertEquals(before.selected,after.selected);v.onEvent(GameEvent.Discard);repeat(100){v.onEvent(GameEvent.Tick(.05f))};assertEquals(FoodPhase.EMPTY,v.state.value.phase);assertEquals(before.served,v.state.value.served)}
 @Test fun autoIgnoresManualFoodActionsUntilTakeover(){val v=vm();v.onEntry(EntryEvent.Guest);v.onEvent(GameEvent.Take);assertEquals(FoodPhase.EMPTY,v.state.value.phase);v.onEvent(GameEvent.TakeOver);v.onEvent(GameEvent.Take);assertEquals(FoodPhase.RAW,v.state.value.phase)}
 @Test fun pausedDemoDoesNotMove(){val v=vm();v.onEntry(EntryEvent.Guest);v.onEvent(GameEvent.Pause);val before=v.state.value;repeat(100){v.onEvent(GameEvent.Tick(.05f))};assertEquals(before,v.state.value)}
 @Test fun resetPreservesControlMode(){val v=vm();v.onEntry(EntryEvent.Guest);v.onEvent(GameEvent.Reset);assertTrue(v.state.value.autoDemo);v.onEvent(GameEvent.TakeOver);v.onEvent(GameEvent.Reset);assertFalse(v.state.value.autoDemo)}
 @Test fun credentialsClearedWhenChangingModeOrEnteringGuest(){val v=vm();v.onEntry(EntryEvent.Password("secret12"));v.onEntry(EntryEvent.Mode(EntryMode.REGISTER));assertEquals("",v.entry.value.password);v.onEntry(EntryEvent.Password("secret12"));v.onEntry(EntryEvent.Guest);assertEquals("",v.entry.value.password)}
 @Test fun submitWithoutForumReportsConfigError()=kotlinx.coroutines.runBlocking{val v=vm();v.onEntry(EntryEvent.Email("a@b.com"));v.onEntry(EntryEvent.Password("secret12"));v.submitAccount();assertFalse(v.entry.value.entered);assertTrue(v.entry.value.error.contains("未配置"))}
}
