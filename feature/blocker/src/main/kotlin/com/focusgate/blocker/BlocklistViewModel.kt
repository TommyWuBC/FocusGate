package com.focusgate.blocker

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusgate.domain.model.BlockedApp
import com.focusgate.domain.repo.BlocklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppRow(val app: BlockedApp, val blocked: Boolean)

data class BlocklistUiState(val rows: List<AppRow> = emptyList(), val loading: Boolean = true)

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: BlocklistRepository,
) : ViewModel() {

    private val installed = MutableStateFlow<List<BlockedApp>?>(null)

    val state: StateFlow<BlocklistUiState> = combine(installed, repo.blockedApps) { all, blocked ->
        if (all == null) BlocklistUiState() else {
            val ids = blocked.map { it.packageName }.toSet()
            // Blocked apps first, then alphabetical.
            BlocklistUiState(
                rows = all.map { AppRow(it, it.packageName in ids) }
                    .sortedWith(compareByDescending<AppRow> { it.blocked }.thenBy { it.app.label.lowercase() }),
                loading = false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BlocklistUiState())

    init { viewModelScope.launch { installed.value = loadInstalled() } }

    fun toggle(row: AppRow) {
        viewModelScope.launch { repo.setBlocked(row.app, !row.blocked) }
    }

    private suspend fun loadInstalled(): List<BlockedApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .map { BlockedApp(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
    }
}
