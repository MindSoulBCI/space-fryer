package com.example.spacefryerdiner.p91d27a30.domain.usecase
import org.junit.Test
import org.junit.Assert.*
import com.example.spacefryerdiner.p91d27a30.domain.model.*
class CookingUseCaseTest {
 private val c=CookingUseCase()
 private fun state()=GameState(listOf(FoodRecipe(1,"corn","popcorn",10f,"pop","expand"),FoodRecipe(2,"chicken","fried",10f,"fry","gold")), started=true)
 @Test fun focusBoundaries(){assertEquals(.2f,c.rate(.299f,false),.001f);assertEquals(1f,c.rate(.30f,false),.001f);assertEquals(2.5f,c.rate(.75f,false),.001f);assertEquals(3.5f,c.rate(1f,false),.001f);assertEquals(3.5f,c.rate(1f,true),.001f)}
 @Test fun highFocusMustBeContinuous(){var s=state().copy(focus=.95f,targetFocus=.95f);s=c.reduce(s,GameEvent.Tick(2.99f));assertFalse(s.overdrive);s=c.reduce(s,GameEvent.Tick(.02f));assertTrue(s.overdrive);s=c.reduce(s.copy(focus=.85f,targetFocus=.85f),GameEvent.Tick(.1f));assertFalse(s.overdrive)}
 @Test fun burnExactlyOnce(){var s=state().copy(phase=FoodPhase.READY,burnAge=2.99f);s=c.reduce(s,GameEvent.Tick(.02f));assertEquals(FoodPhase.BURNT,s.phase);assertEquals(2,s.lives);s=c.reduce(s,GameEvent.Tick(1f));assertEquals(2,s.lives)}
 @Test fun liftedFoodDoesNotBurn(){var s=c.reduce(state().copy(phase=FoodPhase.READY,burnAge=2.9f),GameEvent.Lift);s=c.reduce(s,GameEvent.Tick(5f));assertEquals(FoodPhase.PLATED,s.phase);assertEquals(3,s.lives)}
 @Test fun crossedReadyTimeIsNotLost(){val s=c.reduce(state().copy(phase=FoodPhase.COOKING,progress=.99f,focus=.6f,targetFocus=.6f),GameEvent.Tick(3.2f));assertEquals(FoodPhase.BURNT,s.phase)}
 @Test fun disconnectedAndPausedFreeze(){val s=state().copy(connection=BciConnection.DISCONNECTED,phase=FoodPhase.READY);assertEquals(s,c.reduce(s,GameEvent.Tick(10f)));val p=state().copy(paused=true);assertEquals(p,c.reduce(p,GameEvent.Tick(10f)))}
 @Test fun wrongOrderCannotScore(){val s=state().copy(selected=1,phase=FoodPhase.PLATED);assertEquals(0,c.reduce(s,GameEvent.Serve).coins)}
 @Test fun serveAwardsFlowBonus(){val s=c.reduce(state().copy(phase=FoodPhase.PLATED,highFocusSeconds=3f),GameEvent.Serve);assertEquals(20,s.coins);assertEquals(1,s.order);assertEquals(1,s.combo);assertEquals("S",s.grade)}
 @Test fun timeoutClearsFoodAndAdvances(){val s=c.reduce(state().copy(remaining=.01f,phase=FoodPhase.COOKING),GameEvent.Tick(.1f));assertEquals(2,s.lives);assertEquals(1,s.order);assertEquals(FoodPhase.EMPTY,s.phase)}

 @Test fun overdriveNeverSlowsHighFocus(){for(i in 0..100){val f=i/100f;assertTrue(c.rate(f,true)>=c.rate(f,false))}}
 @Test fun focusChangesBeforeTakingFoodWithoutStartingTimer(){val s=state().copy(started=false,focus=.2f,targetFocus=.95f);val next=c.reduce(s,GameEvent.Tick(.5f));assertTrue(next.focus>s.focus);assertEquals(s.remaining,next.remaining,0f);assertFalse(next.overdrive)}

 @Test fun lowMoodSlowsCookingAtEqualFocus(){val happy=state().copy(phase=FoodPhase.COOKING,focus=.6f,targetFocus=.6f);val sad=happy.copy(mood=0f,targetMood=0f);val h=c.reduce(happy,GameEvent.Tick(1f));val l=c.reduce(sad,GameEvent.Tick(1f));assertEquals(h.progress*.35f,l.progress,.0001f);assertEquals(h.remaining,l.remaining,0f)}
 @Test fun moodRateIsMonotonicAndBounded(){var last=0f;for(i in 0..100){val rate=c.rate(.9f,true,i/100f);assertTrue(rate>=last);last=rate};assertEquals(c.rate(.9f,true,0f),c.rate(.9f,true,-1f),0f);assertEquals(c.rate(.9f,true,1f),c.rate(.9f,true,2f),0f)}
 @Test fun moodInputDoesNotPretendToConnectDevice(){val initial=state();val changed=c.reduce(initial,GameEvent.SetMood(0f));assertEquals(BciConnection.MOCK,changed.connection);assertEquals(0f,changed.targetMood,0f);assertEquals(initial,c.reduce(initial,GameEvent.SetMood(Float.NaN)))}
}
