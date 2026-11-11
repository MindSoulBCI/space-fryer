package com.example.spacefryerdiner.p91d27a30.domain.eeg
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.*
class DemoEegTest {
 @Test fun allChannelsAreFiniteAndDistinct(){val d=DemoEeg();d.advance(16,.8f);val a=d.snapshot();assertEquals(32,a.size);assertTrue(a.all{it.size==512&&it.all{v->v.isFinite()}});assertFalse(a[0].contentEquals(a[1]))}
 @Test fun tenHertzFallsInAlphaBand(){val samples=FloatArray(512){(20*sin(2*PI*10*it/256)).toFloat()};val stats=eegStats(samples);assertEquals(14.14f,stats.rms,.05f);assertTrue(stats.bands[2]>99f);assertEquals(100f,stats.bands.sum(),.01f)}
 @Test fun flatSignalHasNoBandPower(){val stats=eegStats(FloatArray(512));assertEquals(0f,stats.rms,0f);assertTrue(stats.bands.all{it==0f})}
}
