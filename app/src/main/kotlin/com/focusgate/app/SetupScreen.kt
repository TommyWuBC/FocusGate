package com.focusgate.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.focusgate.detection.FocusAccessibilityService

private fun overlayGranted(c: Context) = Settings.canDrawOverlays(c)

private fun accessibilityEnabled(c: Context): Boolean {
    val enabled = Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
    val me = ComponentName(c, FocusAccessibilityService::class.java).flattenToString()
    return enabled.split(':').any { it.equals(me, ignoreCase = true) }
}

@Composable
fun SetupScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var overlay by remember { mutableStateOf(overlayGranted(context)) }
    var access by remember { mutableStateOf(accessibilityEnabled(context)) }

    // Permissions are granted in system Settings, so re-check whenever we come back.
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) { overlay = overlayGranted(context); access = accessibilityEnabled(context) }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Setup", style = MaterialTheme.typography.headlineMedium)
        PermissionCard(
            title = "Display over other apps", granted = overlay,
            body = "Lets the interrupt screen appear on top of the apps you pick.",
            action = "Open settings",
        ) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        PermissionCard(
            title = "Accessibility service", granted = access,
            body = "Detects which app is in the foreground. It does not read screen content.\n\n" +
                "Android 13+ greys this out for sideloaded apps. First open App info > ⋮ menu > \"Allow restricted settings\", then enable the service.",
            action = "Open accessibility settings",
        ) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        OutlinedButton(onClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }) { Text("Open App info (for \"Allow restricted settings\")") }
        Text(
            "Some phones kill background services aggressively. If the interrupt stops appearing, set this app's battery usage to Unrestricted.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PermissionCard(title: String, granted: Boolean, body: String, action: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (granted) "✓ $title" else title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            if (!granted) Button(onClick = onClick) { Text(action) }
        }
    }
}
