package com.example.spacefryerdiner.p91d27a30.ui.game

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.example.spacefryerdiner.p91d27a30.ui.game.*
import com.example.spacefryerdiner.p91d27a30.ui.game.components.*
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.*
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator

fun registerGameWindows(scope: SpatialAppScope, vm: GameViewModel) = with(scope) {
    WindowContainer("DinerOrders", form = Form.Planar,
        defaultSize = WindowContainerSize(680.dp, 860.dp)) {
        PicoTheme {
            val state by vm.state.collectAsState()
            val navigator = LocalSpatialNavigator.current
            val entry by vm.entry.collectAsState()
            OrderPanel(state, entry, vm::onEvent) {
                navigator.openWindowContainer("DinerControls", "controls")
                navigator.openWindowContainer("DinerFocus", "focus")
            }
        }
    }
    WindowContainer("DinerAccount", form = Form.Planar,
        defaultSize = WindowContainerSize(560.dp, 720.dp),
        placement = { containers.firstOrNull { it.id == "DinerOrders" }
            ?.let { Placement.left(it, 80.dp) } ?: Placement.none() }) {
        PicoTheme {
            val entry by vm.entry.collectAsState()
            val navigator = LocalSpatialNavigator.current
            val jobs = rememberCoroutineScope()
            LaunchedEffect(entry.entered) { if (entry.entered) navigator.closeWindowContainer("DinerAccount", "account") }
            if (!entry.entered) EntryPanel(
                entry,
                vm::onEntry,
                submit = { jobs.launch { vm.submitAccount() } },
                sendCode = { jobs.launch { vm.sendCode() } }
            )
        }
    }
    WindowContainer("DinerControls", form = Form.Planar,
        defaultSize = WindowContainerSize(760.dp, 640.dp),
        placement = { containers.firstOrNull { it.id == "DinerOrders" }
            ?.let { Placement.left(it, 120.dp) } ?: Placement.none() }) {
        PicoTheme {
            val state by vm.state.collectAsState()
            ControlPanel(state, vm::onEvent)
        }
    }
    WindowContainer("DinerFocus", form = Form.Planar,
        defaultSize = WindowContainerSize(680.dp, 860.dp),
        placement = { containers.firstOrNull { it.id == "DinerOrders" }
            ?.let { Placement.right(it, 120.dp) } ?: Placement.none() }) {
        PicoTheme {
            val state by vm.state.collectAsState()
            val navigator = LocalSpatialNavigator.current
            BciPanel(state, vm::onEvent) { navigator.openWindowContainer("DinerEeg", "eeg") }
        }
    }
    WindowContainer("DinerEeg", form = Form.Planar,
        defaultSize = WindowContainerSize(1040.dp, 980.dp),
        placement = { containers.firstOrNull { it.id == "DinerFocus" }
            ?.let { Placement.right(it, 120.dp) } ?: Placement.none() }) {
        PicoTheme {
            val state by vm.state.collectAsState()
            EegPanel(state)
        }
    }
}
