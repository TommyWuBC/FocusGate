package com.focusgate.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focusgate.domain.detect.InterruptResult
import com.focusgate.domain.model.Clip
import com.focusgate.domain.model.InterruptConfig
import com.focusgate.domain.usecase.continueSecondsRemaining
import kotlinx.coroutines.delay

private val REASONS = listOf("Messaging someone", "Looking something up", "Work / study", "Just bored")

private enum class Phase { Clip, Friction }

@Composable
fun InterruptScreen(
    appLabel: String,
    clip: Clip?,
    config: InterruptConfig,
    onResult: (InterruptResult) -> Unit,
) {
    var phase by remember { mutableStateOf(Phase.Clip) }
    LaunchedEffect(Unit) {
        delay(config.clipLengthMs)
        phase = Phase.Friction
    }
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.96f)).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (phase) {
            Phase.Clip -> ClipPhase(clip)
            Phase.Friction -> if (config.frictionEnabled) {
                FrictionPhase(appLabel, config.continueDelayMs, onResult)
            } else {
                GoBackOnly(onResult)
            }
        }
    }
}

@Composable
private fun ClipPhase(clip: Clip?) {
    if (clip != null) {
        ClipPlayer(clip.uri, Modifier.fillMaxSize())
    } else {
        Text("Take a breath.", color = Color.White, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun GoBackOnly(onResult: (InterruptResult) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Take a breath.", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { onResult(InterruptResult.GoBack) }) { Text("Go back") }
    }
}

@Composable
private fun FrictionPhase(appLabel: String, delayMs: Long, onResult: (InterruptResult) -> Unit) {
    var reason by remember { mutableStateOf<String?>(null) }
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (elapsed < delayMs) { delay(250); elapsed += 250 }
    }
    val remaining = continueSecondsRemaining(delayMs, elapsed)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "What are you opening $appLabel for?",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        REASONS.forEach { r ->
            FilterChip(selected = reason == r, onClick = { reason = r }, label = { Text(r) })
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onResult(InterruptResult.GoBack) }, modifier = Modifier.fillMaxWidth()) { Text("Go back") }
        OutlinedButton(
            onClick = { onResult(InterruptResult.Continue(reason)) },
            enabled = remaining == 0,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (remaining == 0) "Continue anyway" else "Continue anyway (${remaining}s)") }
    }
}
