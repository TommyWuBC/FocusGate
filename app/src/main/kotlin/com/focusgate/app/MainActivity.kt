package com.focusgate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.focusgate.blocker.BlocklistScreen
import com.focusgate.overlay.ClipsScreen
import com.focusgate.overlay.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

private enum class Tab(val title: String) { Setup("Setup"), Apps("Apps"), Clips("Clips"), Settings("Settings") }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var tab by remember { mutableStateOf(Tab.Setup) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            Tab.entries.forEach {
                                NavigationBarItem(
                                    selected = tab == it, onClick = { tab = it },
                                    icon = {}, label = { Text(it.title) }, alwaysShowLabel = true,
                                )
                            }
                        }
                    },
                ) { padding ->
                    val m = Modifier.padding(padding)
                    when (tab) {
                        Tab.Setup -> SetupScreen(m)
                        Tab.Apps -> BlocklistScreen(m)
                        Tab.Clips -> ClipsScreen(m)
                        Tab.Settings -> SettingsScreen(m)
                    }
                }
            }
        }
    }
}
