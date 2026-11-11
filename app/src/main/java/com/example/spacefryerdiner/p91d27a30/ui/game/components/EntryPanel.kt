package com.example.spacefryerdiner.p91d27a30.ui.game.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.spacefryerdiner.p91d27a30.R
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import com.pico.spatial.ui.design.*
import kotlinx.coroutines.delay

@Composable fun EntryPanel(
 s:EntryState,
 send:(EntryEvent)->Unit,
 submit:()->Unit,
 sendCode:()->Unit
) {
 val register=s.mode==EntryMode.REGISTER
 LaunchedEffect(s.cooldown){if(s.cooldown>0){delay(1000);send(EntryEvent.TickCooldown)}}
 Column(
  Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
  horizontalAlignment=Alignment.CenterHorizontally,
  verticalArrangement=Arrangement.spacedBy(16.dp)
 ) {
  Image(painterResource(R.mipmap.ic_spatial_launcher),"空间油炸食堂",Modifier.size(96.dp))
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
   ToggleableChip(label={Text("登录")}, isToggleOn=!register, onClick={send(EntryEvent.Mode(EntryMode.PASSWORD))})
   ToggleableChip(label={Text("注册")}, isToggleOn=register, onClick={send(EntryEvent.Mode(EntryMode.REGISTER))})
  }
  TextField(value=s.email,onValueChange={send(EntryEvent.Email(it))},placeholder={Text("邮箱")},singleLine=true,enabled=!s.busy,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),modifier=Modifier.fillMaxWidth())
  TextField(value=s.password,onValueChange={send(EntryEvent.Password(it))},placeholder={Text(if(register)"密码，6–20 位字母或数字" else "密码")},singleLine=true,enabled=!s.busy,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),modifier=Modifier.fillMaxWidth())
  if(register){
   TextField(value=s.confirm,onValueChange={send(EntryEvent.Confirm(it))},placeholder={Text("再次输入密码")},singleLine=true,enabled=!s.busy,visualTransformation=PasswordVisualTransformation(),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),modifier=Modifier.fillMaxWidth())
   Row(horizontalArrangement=Arrangement.spacedBy(12.dp), verticalAlignment=Alignment.CenterVertically){
    TextField(value=s.code,onValueChange={send(EntryEvent.Code(it))},placeholder={Text("邮箱验证码")},singleLine=true,enabled=!s.busy,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.weight(1f))
    Button(enabled=!s.busy&&s.cooldown==0,onClick=sendCode){Text(if(s.cooldown>0)"${s.cooldown}s" else "获取验证码")}
   }
  }
  if(s.error.isNotEmpty()) Text(s.error)
  Button(onClick={send(EntryEvent.Mode(if(register) EntryMode.REGISTER else EntryMode.PASSWORD));submit()},enabled=!s.busy){
   Text(if(s.busy)"处理中…" else if(register)"注册" else "登录")
  }
  Button(onClick={send(EntryEvent.Guest)},enabled=!s.busy){Text("游客体验")}
 }
}
