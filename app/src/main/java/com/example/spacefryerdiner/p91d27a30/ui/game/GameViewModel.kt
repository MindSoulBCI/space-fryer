package com.example.spacefryerdiner.p91d27a30.ui.game
import androidx.lifecycle.ViewModel
import com.example.spacefryerdiner.p91d27a30.account.AuthFailure
import com.example.spacefryerdiner.p91d27a30.account.ForumAuthClient
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import com.example.spacefryerdiner.p91d27a30.domain.usecase.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
 recipes: List<FoodRecipe>,
 private val cooker:CookingUseCase=CookingUseCase(),
 private val forum:ForumAuthClient?=null,
 private val demo:GuestDemoUseCase=GuestDemoUseCase()
) : ViewModel() {
 private val mutable=MutableStateFlow(GameState(recipes))
 val state=mutable.asStateFlow()
 private val entryMutable=MutableStateFlow(EntryState())
 val entry=entryMutable.asStateFlow()

 fun onEntry(event:EntryEvent) {
  val e=entryMutable.value
  if(e.entered)return
  if(e.busy && event !is EntryEvent.TickCooldown)return
  entryMutable.value=when(event) {
   is EntryEvent.Mode->e.copy(mode=event.value,password="",confirm="",code="",error="",showPassword=false)
   is EntryEvent.Email->e.copy(email=event.value.take(254),error="")
   is EntryEvent.Password->e.copy(password=event.value.take(20),error="")
   is EntryEvent.Confirm->e.copy(confirm=event.value.take(20),error="")
   is EntryEvent.Code->e.copy(code=event.value.filter(Char::isDigit).take(6),error="")
   EntryEvent.TogglePassword->e.copy(showPassword=!e.showPassword)
   EntryEvent.TickCooldown->if(e.cooldown>0) e.copy(cooldown=e.cooldown-1) else e
   EntryEvent.Guest->{
    mutable.value=mutable.value.copy(autoDemo=true,status="游客自动演示 · 可随时接管操作")
    EntryState(entered=true,guest=true,name="游客")
   }
  }
 }

 suspend fun sendCode() {
  val e=entryMutable.value
  if(e.busy || e.entered || e.cooldown>0)return
  val client=forum ?: run {
   entryMutable.value=e.copy(error="账号服务未配置")
   return
  }
  entryMutable.value=e.copy(busy=true,error="")
  try {
   val mail=e.email.trim()
   if(!ForumAuthClient.validEmail(mail)) throw AuthFailure("请输入有效邮箱")
   client.sendCode(mail)
   entryMutable.value=entryMutable.value.copy(busy=false,cooldown=60,error="验证码已发送，请查收邮箱")
  } catch(ex:CancellationException) {
   entryMutable.value=e.copy(busy=false); throw ex
  } catch(ex:Exception) {
   entryMutable.value=e.copy(busy=false,error=ex.message?:"发送失败，请重试")
  }
 }

 suspend fun submitAccount() {
  val e=entryMutable.value
  if(e.busy || e.entered)return
  val client=forum ?: run {
   entryMutable.value=e.copy(error="账号服务未配置")
   return
  }
  entryMutable.value=e.copy(busy=true,error="")
  try {
   val mail=e.email.trim()
   if(!ForumAuthClient.validEmail(mail)) throw AuthFailure("请输入有效邮箱")
   val session=when(e.mode){
    EntryMode.PASSWORD->{
     if(e.password.isEmpty()) throw AuthFailure("请输入密码")
     client.login(mail,e.password)
    }
    EntryMode.CODE->{
     if(!ForumAuthClient.validCode(e.code)) throw AuthFailure("请输入 6 位验证码")
     client.loginByCode(mail,e.code)
    }
    EntryMode.REGISTER->{
     if(!ForumAuthClient.validPassword(e.password)) throw AuthFailure("密码需为 6–20 位字母或数字")
     if(e.password!=e.confirm) throw AuthFailure("两次密码不一致")
     if(!ForumAuthClient.validCode(e.code)) throw AuthFailure("请输入 6 位验证码")
     client.register(mail,e.password,e.code)
    }
   }
   entryMutable.value=EntryState(entered=true,name=session.nickname.ifBlank{session.email.substringBefore('@')})
  } catch(ex:CancellationException) {
   entryMutable.value=e.copy(password="",confirm="",code=""); throw ex
  } catch(ex:Exception) {
   entryMutable.value=e.copy(busy=false,password="",confirm="",code="",error=ex.message?:"登录失败，请重试")
  }
 }

 fun onEvent(event:GameEvent) {
  if(!entryMutable.value.entered)return
  val s=mutable.value
  if(event==GameEvent.TakeOver) { mutable.value=s.copy(autoDemo=false,status="已接管 · 当前食物与进度已保留");return }
  if(s.autoDemo && (event is GameEvent.Select || event in listOf(GameEvent.Take,GameEvent.Drop,GameEvent.Lift,GameEvent.Serve,GameEvent.Discard)))return
  val next=cooker.reduce(s,event)
  mutable.value=if(event is GameEvent.Tick)demo.advance(next,event.seconds,cooker) else next
 }
 fun walk(delta:Float) { val s=mutable.value;if(entryMutable.value.entered&&s.mockWalk&&!s.suspended)mutable.value=s.copy(targetFocus=(s.targetFocus+delta).coerceIn(.05f,.99f)) }
}
