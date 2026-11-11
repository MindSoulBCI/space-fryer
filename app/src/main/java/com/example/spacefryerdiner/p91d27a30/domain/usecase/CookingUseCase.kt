package com.example.spacefryerdiner.p91d27a30.domain.usecase

import com.example.spacefryerdiner.p91d27a30.domain.model.*
import kotlin.math.*

class CookingUseCase {
 fun rate(focus: Float, overdrive: Boolean, mood:Float=1f):Float {
  val base=when {focus<.30f->.2f;focus<.75f->1f;else->2.5f+(focus-.75f)*4f}
  return (if(overdrive)max(3f,base) else base) * moodFactor(mood)
 }
 fun moodFactor(mood:Float)=.35f+.65f*mood.coerceIn(0f,1f)
 fun reduce(s: GameState, event: GameEvent): GameState {
  if(event == GameEvent.Reset) return GameState(s.recipes,autoDemo=s.autoDemo)
  if(event == GameEvent.Pause) return s.copy(paused=!s.paused)
  if(event == GameEvent.ToggleConnection) return s.copy(connection=if(s.connection==BciConnection.MOCK) BciConnection.DISCONNECTED else BciConnection.MOCK, highFocusSeconds=0f, signal=if(s.connection==BciConnection.MOCK)0 else 4)
  if(event is GameEvent.SetFocus) return s.copy(targetFocus=event.value.coerceIn(0f,1f),mockWalk=false)
  if(event is GameEvent.SetMood)return if(event.value.isFinite())s.copy(targetMood=event.value.coerceIn(0f,1f)) else s
  if(event == GameEvent.ToggleWalk) return s.copy(mockWalk=!s.mockWalk)
  if(s.suspended) return s
  return when(event) {
   is GameEvent.Select -> if(s.phase==FoodPhase.EMPTY) s.copy(selected=event.index.coerceIn(s.recipes.indices)) else s
   GameEvent.Take -> if(s.phase==FoodPhase.EMPTY) s.copy(started=true,phase=FoodPhase.RAW,status="已取 ${s.recipe.raw}，可以下锅") else s
   GameEvent.Drop -> if(s.phase==FoodPhase.RAW) s.copy(phase=FoodPhase.COOKING,progress=0f,burnAge=0f,soggy=false,status="油炸中，注意火候") else s
   GameEvent.Lift -> if(s.phase==FoodPhase.READY) s.copy(phase=FoodPhase.PLATED,status="已捞出，请出餐") else s.copy(status="还没有成熟，请稍候")
   GameEvent.Discard -> s.copy(phase=FoodPhase.EMPTY,progress=0f,burnAge=0f,soggy=false,status="已清理，重新取料")
   GameEvent.Serve -> if(s.phase!=FoodPhase.PLATED) s else if(s.selected!=s.order) s.copy(status="食材不匹配：客人要 ${s.wanted.cooked}") else {
    val grade=when {s.remaining>45 -> "S";s.remaining>30 -> "A";s.remaining>15 -> "B";else -> "C"}
    s.copy(phase=FoodPhase.EMPTY,progress=0f,burnAge=0f,remaining=60f,order=(s.order+1)%s.recipes.size,selected=(s.order+1)%s.recipes.size,
     coins=s.coins+10+s.combo*2+(if(s.overdrive)10 else 0),combo=s.combo+1,served=s.served+1,grade=grade,status="$grade 级出餐！"+(if(s.overdrive)" 心流奖励 +10" else ""))
   }
   is GameEvent.Tick -> tick(s,event.seconds)
   else -> s
  }
 }
 private fun tick(s:GameState,dt:Float):GameState {
  if(!dt.isFinite() || dt<=0f) return s
  val focus=s.focus+(s.targetFocus-s.focus)*(1-exp(-dt*3f))
  val mood=s.mood+(s.targetMood-s.mood)*(1-exp(-dt*2f))
  if(!s.started)return s.copy(focus=focus,mood=mood)
  val high=if(focus>.85f)s.highFocusSeconds+dt else 0f
  var n=s.copy(focus=focus,mood=mood,highFocusSeconds=high,remaining=max(0f,s.remaining-dt))
  if(n.remaining==0f) return n.copy(lives=max(0,s.lives-1),combo=0,grade="F",remaining=60f,phase=FoodPhase.EMPTY,progress=0f,burnAge=0f,order=(s.order+1)%s.recipes.size,selected=(s.order+1)%s.recipes.size,status="订单超时，失去一颗生命")
  if(s.phase==FoodPhase.COOKING) {
   val speed=rate(focus,n.overdrive,mood)/s.recipe.seconds
   val untilReady=(1-s.progress)/speed
   n=n.copy(progress=min(1f,s.progress+dt*speed),soggy=s.soggy||focus<.25f)
   if(dt>=untilReady)n=n.copy(phase=FoodPhase.READY,burnAge=max(0f,dt-untilReady),status="熟了！3秒内捞起")
  } else if(s.phase==FoodPhase.READY)n=n.copy(burnAge=s.burnAge+dt)
  if(n.phase==FoodPhase.READY && n.burnAge>=3f)n=n.copy(phase=FoodPhase.BURNT,lives=max(0,n.lives-1),combo=0,grade="F",status="焦糊了！清理后继续")
  return n
 }
}
