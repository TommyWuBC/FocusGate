package com.focusgate.blocker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BlocklistScreen(modifier: Modifier = Modifier, vm: BlocklistViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    if (state.loading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(modifier.fillMaxSize()) {
        items(state.rows, key = { it.app.packageName }) { row ->
            Row(
                Modifier.fillMaxWidth().clickable { vm.toggle(row) }.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(row.app.label, style = MaterialTheme.typography.bodyLarge)
                    Text(row.app.packageName, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = row.blocked, onCheckedChange = { vm.toggle(row) })
            }
            HorizontalDivider()
        }
    }
}
