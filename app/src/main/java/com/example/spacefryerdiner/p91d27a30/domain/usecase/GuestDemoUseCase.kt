package com.example.spacefryerdiner.p91d27a30.domain.usecase
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import kotlin.random.Random
/** One action per beat: no delayed action survives manual takeover. */
class GuestDemoUseCase(private val random:Random=Random.Default) {
 fun advance(s:GameState,dt:Float,cooker:CookingUseCase):GameState {
  if(!s.autoDemo || s.suspended || !dt.isFinite() || dt<=0f)return s
  val elapsed=s.demoElapsed+dt
  if(elapsed<.7f)return s.copy(demoElapsed=elapsed)
  val n=s.copy(demoElapsed=0f)
  return when(n.phase) {
   FoodPhase.EMPTY -> {
    val next=random.nextInt(n.recipes.size)
    cooker.reduce(n.copy(selected=next,order=next,remaining=60f),GameEvent.Take)
   }
   FoodPhase.RAW -> cooker.reduce(n,GameEvent.Drop)
   FoodPhase.READY -> cooker.reduce(n,GameEvent.Lift)
   FoodPhase.PLATED -> cooker.reduce(n,GameEvent.Serve)
   FoodPhase.BURNT -> cooker.reduce(n,GameEvent.Discard)
   FoodPhase.COOKING -> n
  }
 }
}
