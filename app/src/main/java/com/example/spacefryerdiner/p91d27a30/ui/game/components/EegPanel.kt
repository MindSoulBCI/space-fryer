package com.example.spacefryerdiner.p91d27a30.ui.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.pico.spatial.ui.design.*
import com.example.spacefryerdiner.p91d27a30.domain.eeg.*
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import kotlinx.coroutines.delay

@Composable fun EegPanel(s:GameState) {
 val source=remember{DemoEeg()}
 var traces by remember{mutableStateOf(source.snapshot())}
 var selected by remember{mutableIntStateOf(0)}
 var frozen by remember{mutableStateOf(false)}
 var stats by remember{mutableStateOf(eegStats(traces[0]))}
 val focus by rememberUpdatedState(s.focus)
 val owner=LocalLifecycleOwner.current
 val connected=s.connection==BciConnection.MOCK
 LaunchedEffect(source,connected,frozen,owner) {
  if(connected&&!frozen)owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
   while(true){delay(63);source.advance(16,focus);traces=source.snapshot()}
  }
 }
 LaunchedEffect(selected,connected,frozen) {
  while(true){stats=eegStats(traces[selected]);delay(1000)}
 }
 val fg=PicoTheme.colorScheme.labelPrimary
 val muted=PicoTheme.colorScheme.labelSecondary
 Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Text("32 通道 EEG · 模拟数据",style=PicoTheme.typography.titleLarge,color=fg)
  Text(if(!connected)"模拟连接已断开 · 波形停止，未接入真实设备" else "模拟采样 256 Hz · 最近 2 秒 · 显示 ±60 μV · 未接入真实设备",color=fg)
  Text("专注力 ${(s.focus*100).toInt()}% / 情绪 ${(s.mood*100).toInt()}% 均为模拟输入，不由 EEG 推断。",color=muted)
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   Button(onClick={selected=(selected+31)%32}){Text("上一通道")}
   Button(onClick={selected=(selected+1)%32}){Text("下一通道")}
   Button(onClick={frozen=!frozen}){Text(if(frozen)"继续波形" else "冻结波形")}
  }
  Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(24.dp)) {
   repeat(2){column->
    Column(Modifier.weight(1f).fillMaxHeight()) {
     repeat(16){row->
      val ch=column*16+row
      Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
       Text((if(ch==selected)"› " else "")+DemoEeg.channels[ch],modifier=Modifier.width(64.dp),color=fg)
       // No native waveform component: transparent scientific trace, native theme foreground.
       Canvas(Modifier.weight(1f).fillMaxHeight()) {
        val path=Path()
        traces[ch].forEachIndexed { i,v ->
         val x=i.toFloat()/(traces[ch].size-1)*size.width
         val y=size.height*(.5f-v.coerceIn(-60f,60f)/120f)
         if(i==0)path.moveTo(x,y) else path.lineTo(x,y)
        }
        drawPath(path,if(ch==selected)fg else muted,style=Stroke(if(ch==selected)2f else 1.2f))
       }
      }
     }
    }
   }
  }
  Text("${DemoEeg.channels[selected]} · RMS ${"%.1f".format(stats.rms)} μV · 峰峰值 ${"%.1f".format(stats.peakToPeak)} μV"+(if(frozen||!connected)" · 已冻结" else ""),color=fg)
  Text("模拟频段占比："+listOf("δ 1–4","θ 4–8","α 8–13","β 13–30","γ 30–45").mapIndexed{i,n->"$n Hz ${stats.bands[i].toInt()}%"}.joinToString("  ·  "),color=fg)
  Text("频段仅用于展示合成信号，不输出脑健康或临床结论。",color=muted)
 }
}
