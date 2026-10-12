package com.focusgate.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusgate.domain.model.Clip
import com.focusgate.domain.repo.ClipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClipsViewModel @Inject constructor(private val repo: ClipRepository) : ViewModel() {
    val clips: StateFlow<List<Clip>> =
        repo.clips.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(uri: String, title: String) { viewModelScope.launch { repo.add(uri, title) } }
    fun remove(clip: Clip) { viewModelScope.launch { repo.remove(clip.id) } }
}
