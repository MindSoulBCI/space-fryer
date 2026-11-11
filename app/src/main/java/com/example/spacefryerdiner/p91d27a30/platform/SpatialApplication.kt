package com.example.spacefryerdiner.p91d27a30.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.spacefryerdiner.p91d27a30.mainApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpatialApplication : Application() {
    private val resumed = mutableSetOf<Activity>()
    private val visible = MutableStateFlow(false)
    val gameVisible = visible.asStateFlow()
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(a: Activity) { resumed.add(a); visible.value=true }
            override fun onActivityPaused(a: Activity) { resumed.remove(a); visible.value=resumed.isNotEmpty() }
            override fun onActivityDestroyed(a: Activity) { resumed.remove(a); visible.value=resumed.isNotEmpty() }
            override fun onActivityCreated(a: Activity, b: Bundle?) {}
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
        })
        launch { mainApp(this, this@SpatialApplication) }
    }
}
