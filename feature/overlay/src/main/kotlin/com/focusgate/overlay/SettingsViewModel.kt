package com.focusgate.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusgate.domain.model.InterruptConfig
import com.focusgate.domain.repo.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    val config: StateFlow<InterruptConfig> =
        repo.interruptConfig.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InterruptConfig())

    fun update(transform: (InterruptConfig) -> InterruptConfig) {
        viewModelScope.launch { repo.update(transform(config.value)) }
    }
}
