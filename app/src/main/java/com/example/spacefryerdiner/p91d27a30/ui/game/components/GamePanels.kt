package com.example.spacefryerdiner.p91d27a30.ui.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.design.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import com.example.spacefryerdiner.p91d27a30.domain.usecase.CookingUseCase

@Composable fun OrderPanel(s:GameState, entry:EntryState, send:(GameEvent)->Unit, openPanels:()->Unit) {
 Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
  Text("空间油炸食堂",style=PicoTheme.typography.titleLarge,color=PicoTheme.colorScheme.labelPrimary)
  Text("${entry.name} · "+(if(s.autoDemo)"游客自动演示" else "手动操作"),color=PicoTheme.colorScheme.labelPrimary)
  if(s.autoDemo)Button(onClick={send(GameEvent.TakeOver)}){Text("接管操作 · 停止自动油炸")}
  Text("外星食客想要：${s.wanted.cooked}",style=PicoTheme.typography.titleMedium,color=PicoTheme.colorScheme.labelPrimary)
  Text("${s.remaining.toInt()} 秒  ·  生命 ${s.lives}/3  ·  金币 ${s.coins}  ·  连击 ${s.combo}",color=PicoTheme.colorScheme.labelPrimary)
  Text(if(s.lives<=0)"营业结束 · 点击重新开店" else s.status,color=PicoTheme.colorScheme.labelPrimary)
  Button(onClick=openPanels){Text("打开选料 / 专注力窗口")}
 }
}
@Composable fun BciPanel(s:GameState,send:(GameEvent)->Unit,openEeg:()->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  Text("专注力 ${(s.focus*100).toInt()}%",style=PicoTheme.typography.titleLarge,color=PicoTheme.colorScheme.labelPrimary)
  Text(if(s.connection==BciConnection.MOCK)"MOCK · 离线演示" else "已断开 · 游戏计时暂停",color=PicoTheme.colorScheme.labelPrimary)
  Text("${s.device} · 32 通道为合成演示，非实测",color=PicoTheme.colorScheme.labelPrimary)
  Text("油炸速度 ×${"%.2f".format(CookingUseCase().rate(s.focus,s.overdrive,s.mood))} · 专注力越高反应越快",color=PicoTheme.colorScheme.labelPrimary)
  Button(onClick=openEeg){Text("打开 32 通道 EEG 看板")}
  Text(if(s.overdrive)"心流爆燃 · 至少3倍速" else "心流蓄力 ${s.highFocusSeconds.coerceAtMost(3f).toString().take(3)} / 3 秒",color=PicoTheme.colorScheme.labelPrimary)
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   Button(onClick={send(GameEvent.SetFocus(.15f))}){Text("低")}
   Button(onClick={send(GameEvent.SetFocus(.60f))}){Text("中")}
   Button(onClick={send(GameEvent.SetFocus(.95f))}){Text("高")}
  }
  Text("模拟情绪："+when{ s.mood<.35f->"低落";s.mood<.75f->"平静";else->"愉快"}+" · 烹饪系数 ×${"%.2f".format(CookingUseCase().moodFactor(s.mood))}",color=PicoTheme.colorScheme.labelPrimary)
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   Button(onClick={send(GameEvent.SetMood(0f))}){Text("低落")}
   Button(onClick={send(GameEvent.SetMood(.5f))}){Text("平静")}
   Button(onClick={send(GameEvent.SetMood(1f))}){Text("愉快")}
  }
  Text("情绪为模拟输入，未启用真实情绪识别。",color=PicoTheme.colorScheme.labelSecondary)
  Button(onClick={send(GameEvent.ToggleWalk)}){Text(if(s.mockWalk)"随机游走：开" else "随机游走：关")}
  Button(onClick={send(GameEvent.ToggleConnection)}){Text(if(s.connection==BciConnection.MOCK)"模拟断连" else "恢复模拟")}
 }
}
@Composable fun ControlPanel(s:GameState,send:(GameEvent)->Unit) {
 Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
  Text("自选食材 · ${s.recipe.raw}",style=PicoTheme.typography.titleLarge,color=PicoTheme.colorScheme.labelPrimary)
  if(s.autoDemo)Button(onClick={send(GameEvent.TakeOver)}){Text("接管操作 · 自己选食材")}
  Text("桌面食材常驻陈列，点选后取料；烹饪中请先出餐或清理。",color=PicoTheme.colorScheme.labelPrimary)
  LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)) {
   items(s.recipes.withIndex().toList().chunked(3)) { row ->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
     row.forEach { (index,recipe) ->
      Button(modifier=Modifier.weight(1f),onClick={send(GameEvent.Select(index))},enabled=!s.autoDemo&&!s.suspended&&s.phase==FoodPhase.EMPTY) {
       Text((if(index==s.selected)"✓ " else "")+recipe.raw)
      }
     }
    }
   }
  }
  Text("烹饪 ${(s.progress*100).toInt()}%" + (if(s.phase==FoodPhase.READY)" · ${String.format("%.1f",(3-s.burnAge).coerceAtLeast(0f))}秒后焦糊" else "") + if(s.soggy)" · 油温低，食材油腻" else "",color=PicoTheme.colorScheme.labelPrimary)
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Button(onClick={send(GameEvent.Take)},enabled=!s.autoDemo&&!s.suspended&&s.phase==FoodPhase.EMPTY){Text("取料")}
   Button(onClick={send(GameEvent.Drop)},enabled=!s.autoDemo&&!s.suspended&&s.phase==FoodPhase.RAW){Text("下锅")}
   Button(onClick={send(GameEvent.Lift)},enabled=!s.autoDemo&&!s.suspended&&s.phase==FoodPhase.READY){Text("捞起")}
   Button(onClick={send(GameEvent.Serve)},enabled=!s.autoDemo&&!s.suspended&&s.phase==FoodPhase.PLATED){Text("出餐")}
  }
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Button(onClick={send(GameEvent.Discard)},enabled=!s.autoDemo&&!s.suspended){Text("清理")}
   Button(onClick={send(GameEvent.Pause)}){Text(if(s.paused)"继续" else "暂停")}
   Button(onClick={send(GameEvent.Reset)}){Text("重新开店")}
  }
  Text("按钮取放 · 真实手势抓取与桌面锚定尚未接入",color=PicoTheme.colorScheme.labelPrimary)
 }
}
