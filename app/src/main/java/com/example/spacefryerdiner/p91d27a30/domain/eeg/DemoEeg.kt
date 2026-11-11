package com.example.spacefryerdiner.p91d27a30.domain.eeg

import kotlin.math.*

/** Synthetic microvolt signals only. This is not a device driver or attention estimator. */
class DemoEeg {
 companion object {
  const val SAMPLE_RATE=256
  const val WINDOW=512
  val channels=listOf("Fp1","Fp2","F7","F3","Fz","F4","F8","FC5","FC1","FC2","FC6","T7","C3","Cz","C4","T8","CP5","CP1","CP2","CP6","P7","P3","Pz","P4","P8","PO9","O1","Oz","O2","PO10","AF3","AF4")
 }
 private val samples=Array(32){FloatArray(WINDOW)}
 private var cursor=0
 private var total=0L
 init { advance(WINDOW,.65f) }
 fun advance(count:Int,focus:Float) {
  repeat(count.coerceIn(0,WINDOW)) {
   val t=total.toDouble()/SAMPLE_RATE
   for(ch in 0..31) {
    val phase=ch*.37
    // Deliberately illustrative mixtures; changing the demo slider is not a physiological inference.
    samples[ch][cursor]=((18-8*focus)*sin(2*PI*10*t+phase)+(5+10*focus)*sin(2*PI*20*t+phase*.5)+6*sin(2*PI*5*t+phase)+3*sin(2*PI*2*t+phase*.3)+2*sin(2*PI*37*t+phase)).toFloat()
   }
   total++;cursor=(cursor+1)%WINDOW
  }
 }
 fun snapshot()=Array(32){ch->FloatArray(WINDOW){i->samples[ch][(cursor+i)%WINDOW]}}
}
data class EegStats(val rms:Float,val peakToPeak:Float,val bands:List<Float>)

/** Hann-windowed DFT for a two-second illustrative spectrum, 1–45 Hz normalized power. */
fun eegStats(samples:FloatArray,rate:Int=DemoEeg.SAMPLE_RATE):EegStats {
 if(samples.isEmpty())return EegStats(0f,0f,List(5){0f})
 val mean=samples.average()
 val rms=sqrt(samples.sumOf { it.toDouble()*it }/samples.size).toFloat()
 val bands=DoubleArray(5)
 for(k in 1..samples.size/2) {
  val hz=k.toDouble()*rate/samples.size
  if(hz<1||hz>45)continue
  var re=0.0;var im=0.0
  for(i in samples.indices) {
   val value=(samples[i]-mean)*(.5-.5*cos(2*PI*i/(samples.size-1)))
   val angle=2*PI*k*i/samples.size
   re+=value*cos(angle);im-=value*sin(angle)
  }
  val band=when {hz<4->0;hz<8->1;hz<13->2;hz<30->3;else->4}
  bands[band]+=re*re+im*im
 }
 val total=bands.sum()
 return EegStats(rms,(samples.maxOrNull()?:0f)-(samples.minOrNull()?:0f),bands.map { if(total==0.0)0f else (it/total*100).toFloat() })
}
