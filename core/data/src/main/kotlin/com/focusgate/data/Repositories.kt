package com.focusgate.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.focusgate.domain.model.*
import com.focusgate.domain.repo.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class RoomBlocklistRepository @Inject constructor(private val dao: BlockedAppDao) : BlocklistRepository {
    override val blockedApps = dao.observe().map { l -> l.map { BlockedApp(it.packageName, it.label) } }
    override suspend fun setBlocked(app: BlockedApp, blocked: Boolean) =
        if (blocked) dao.insert(BlockedAppEntity(app.packageName, app.label)) else dao.delete(app.packageName)
}

class RoomClipRepository @Inject constructor(
    private val dao: ClipDao,
    private val prefs: DataStore<Preferences>,
) : ClipRepository {
    private val lastKey = longPreferencesKey("last_clip_id")
    override val clips = dao.observe().map { l -> l.map { Clip(it.id, it.uri, it.title) } }
    override suspend fun add(uri: String, title: String) { dao.insert(ClipEntity(uri = uri, title = title)) }
    override suspend fun remove(id: Long) = dao.delete(id)
    override suspend fun lastPlayedId() = prefs.data.first()[lastKey]
    override suspend fun setLastPlayed(id: Long) { prefs.edit { it[lastKey] = id } }
}

class DataStoreSettingsRepository @Inject constructor(private val prefs: DataStore<Preferences>) : SettingsRepository {
    private val clipMs = longPreferencesKey("clip_length_ms")
    private val friction = booleanPreferencesKey("friction_enabled")
    private val delay = longPreferencesKey("continue_delay_ms")
    private val d = InterruptConfig()

    override val interruptConfig = prefs.data.map {
        InterruptConfig(it[clipMs] ?: d.clipLengthMs, it[friction] ?: d.frictionEnabled, it[delay] ?: d.continueDelayMs)
    }

    override suspend fun update(config: InterruptConfig) {
        prefs.edit {
            it[clipMs] = config.clipLengthMs
            it[friction] = config.frictionEnabled
            it[delay] = config.continueDelayMs
        }
    }
}
