package com.focusgate.overlay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, vm: SettingsViewModel = viewModel()) {
    val config by vm.config.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Clip length: ${config.clipLengthMs / 1000}s")
        Slider(
            value = config.clipLengthMs / 1000f, valueRange = 1f..30f, steps = 28,
            onValueChange = { s -> vm.update { it.copy(clipLengthMs = s.toLong() * 1000) } },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ask why after the clip", Modifier.weight(1f))
            Switch(checked = config.frictionEnabled, onCheckedChange = { on -> vm.update { it.copy(frictionEnabled = on) } })
        }
        if (config.frictionEnabled) {
            Text("\"Continue anyway\" delay: ${config.continueDelayMs / 1000}s")
            Slider(
                value = config.continueDelayMs / 1000f, valueRange = 0f..120f, steps = 23,
                onValueChange = { s -> vm.update { it.copy(continueDelayMs = (s / 5).toLong() * 5000) } },
            )
        }
    }
}
