package com.example.spacefryerdiner.p91d27a30.ui.game
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.spacefryerdiner.p91d27a30.R
import com.example.spacefryerdiner.p91d27a30.domain.model.*
/** CC0 real frying loop; readiness and burn cues remain procedural. */
class AudioManager(context:Context) {
 private val pool=SoundPool.Builder().setMaxStreams(4).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build()).build()
 private val loaded=java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
 init { pool.setOnLoadCompleteListener { _,id,status -> if(status==0)loaded.add(id) } }
 private val sizzle=pool.load(context,R.raw.sizzle,1)
 private val ready=pool.load(context,R.raw.ready,1)
 private val burn=pool.load(context,R.raw.burn,1)
 private var stream=0;private var volume=0f;private var phase=FoodPhase.EMPTY
 fun update(s:GameState,active:Boolean) {
  val cooking=active&&!s.suspended&&(s.phase==FoodPhase.COOKING||s.phase==FoodPhase.READY)
  if(cooking&&stream==0&&sizzle in loaded)stream=pool.play(sizzle,0f,0f,1,-1,1f)
  val goal=if(cooking).08f+.24f*s.focus else 0f
  volume+=(goal-volume)*.15f
  if(stream!=0){pool.setVolume(stream,volume,volume);pool.setRate(stream,.90f+s.focus*.20f);if(!cooking&&volume<.005f){pool.stop(stream);stream=0}}
  if(active&&!s.suspended&&s.phase!=phase){if(s.phase==FoodPhase.READY&&ready in loaded)pool.play(ready,.3f,.3f,2,0,1f);if(s.phase==FoodPhase.BURNT&&burn in loaded)pool.play(burn,.3f,.3f,2,0,1f)}
  phase=s.phase
 }
 fun silence(){if(stream!=0)pool.stop(stream);stream=0;volume=0f}
 fun close(){pool.release()}
}
