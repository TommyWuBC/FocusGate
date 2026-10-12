package com.focusgate.domain.repo

import com.focusgate.domain.model.BlockedApp
import com.focusgate.domain.model.Clip
import com.focusgate.domain.model.InterruptConfig
import kotlinx.coroutines.flow.Flow

interface BlocklistRepository {
    val blockedApps: Flow<List<BlockedApp>>
    suspend fun setBlocked(app: BlockedApp, blocked: Boolean)
}

interface ClipRepository {
    val clips: Flow<List<Clip>>
    suspend fun add(uri: String, title: String)
    suspend fun remove(id: Long)
    /** Id of the clip played most recently, used to avoid immediate repeats. */
    suspend fun lastPlayedId(): Long?
    suspend fun setLastPlayed(id: Long)
}

interface SettingsRepository {
    val interruptConfig: Flow<InterruptConfig>
    suspend fun update(config: InterruptConfig)
}
