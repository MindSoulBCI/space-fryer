package com.example.spacefryerdiner.p91d27a30.domain.model

enum class EntryMode(val title:String){PASSWORD("密码登录"),CODE("验证码登录"),REGISTER("注册")}

data class EntryState(
 val entered:Boolean=false,
 val guest:Boolean=false,
 val name:String="",
 val mode:EntryMode=EntryMode.PASSWORD,
 val email:String="",
 val password:String="",
 val confirm:String="",
 val code:String="",
 val showPassword:Boolean=false,
 val cooldown:Int=0,
 val busy:Boolean=false,
 val error:String=""
)

sealed interface EntryEvent {
 data class Mode(val value:EntryMode):EntryEvent
 data class Email(val value:String):EntryEvent
 data class Password(val value:String):EntryEvent
 data class Confirm(val value:String):EntryEvent
 data class Code(val value:String):EntryEvent
 data object TogglePassword:EntryEvent
 data object TickCooldown:EntryEvent
 data object Guest:EntryEvent
}
