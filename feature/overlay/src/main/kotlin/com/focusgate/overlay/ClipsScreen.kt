package com.focusgate.overlay

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ClipsScreen(modifier: Modifier = Modifier, vm: ClipsViewModel = viewModel()) {
    val clips by vm.clips.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var link by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        // Keep read access across reboots.
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        vm.add(uri.toString(), uri.lastPathSegment?.substringAfterLast('/') ?: "Clip")
    }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { picker.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Add video from device")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = link, onValueChange = { link = it }, singleLine = true,
                label = { Text("Direct video link (https://…)") }, modifier = Modifier.weight(1f),
            )
            Button(
                enabled = link.startsWith("https://"),
                onClick = { vm.add(link.trim(), link.trim().substringAfterLast('/')); link = "" },
            ) { Text("Add") }
        }
        if (clips.isEmpty()) {
            Text("No clips yet. Until you add some, a built-in sample plays.", style = MaterialTheme.typography.bodyMedium)
        }
        LazyColumn {
            items(clips, key = { it.id }) { clip ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(clip.title, Modifier.weight(1f), maxLines = 1)
                    TextButton(onClick = { vm.remove(clip) }) { Text("Remove") }
                }
                HorizontalDivider()
            }
        }
    }
}
