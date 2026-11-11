package com.example.spacefryerdiner.p91d27a30

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.example.spacefryerdiner.p91d27a30.ui.game.*
import com.example.spacefryerdiner.p91d27a30.ui.game.components.*
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.*
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator

fun mainApp(scope: SpatialAppScope, context: android.content.Context) = with(scope) {
    // One state owner shared by the Stage and all native windows.
    val vm = GameViewModelFactory(context).create(GameViewModel::class.java)
    DefaultStage { PicoTheme { GameScreen(vm) } }
    registerGameWindows(scope, vm)
}
