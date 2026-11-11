package com.example.spacefryerdiner.p91d27a30.ui.game

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import com.example.spacefryerdiner.p91d27a30.platform.SpatialApplication
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable fun GameScreen(vm: GameViewModel) {
 val s by vm.state.collectAsState()
 val entry by vm.entry.collectAsState()
 val scene=remember{FryerScene()}
 val context=LocalContext.current
 val app=context.applicationContext as SpatialApplication
 val active by app.gameVisible.collectAsState()
 val audio=remember{AudioManager(context)}
 val navigator=LocalSpatialNavigator.current
 LaunchedEffect(scene,entry.entered){if(entry.entered)scene.loadAssets()}
 LaunchedEffect(navigator, entry.entered){
  navigator.openWindowContainer("DinerOrders", "orders")
  if(!entry.entered) navigator.openWindowContainer("DinerAccount", "account")
 }
 LaunchedEffect(navigator,entry.entered){
  if(!entry.entered)return@LaunchedEffect
  delay(500) // Register the central window before opening its neighbours.
  navigator.openWindowContainer("DinerControls", "controls")
  navigator.openWindowContainer("DinerFocus", "focus")
  delay(500)
  navigator.openWindowContainer("DinerEeg", "eeg")
 }
 DisposableEffect(scene,audio){onDispose{
  audio.close();scene.close()
  navigator.closeWindowContainer("DinerEeg", "eeg")
  navigator.closeWindowContainer("DinerControls", "controls")
  navigator.closeWindowContainer("DinerFocus", "focus")
  navigator.closeWindowContainer("DinerOrders", "orders")
 }}
 // Focus moving between our own windows must not pause cooking or restart the audio.
 LaunchedEffect(active,entry.entered){
  if(!active || !entry.entered) audio.silence()
  if(active && entry.entered){var ticks=0;while(true){delay(50);vm.onEvent(GameEvent.Tick(.05f));if(++ticks%20==0)vm.walk(Random.nextFloat()*.20f-.10f)}}
 }
 if(entry.entered) SpatialView(modifier=Modifier.fillMaxSize(),initial={content,_->content.addEntity(scene.root)},
  update={_,_->scene.update(s,60f-s.remaining);audio.update(s,active)})
}
