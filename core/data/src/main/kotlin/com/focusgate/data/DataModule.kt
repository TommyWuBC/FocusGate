package com.focusgate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.focusgate.domain.repo.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("focusgate_prefs")

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindings {
    @Binds abstract fun blocklist(i: RoomBlocklistRepository): BlocklistRepository
    @Binds abstract fun clips(i: RoomClipRepository): ClipRepository
    @Binds abstract fun settings(i: DataStoreSettingsRepository): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvides {
    @Provides @Singleton
    fun db(@ApplicationContext c: Context): AppDatabase =
        Room.databaseBuilder(c, AppDatabase::class.java, "focusgate.db")
            .addMigrations(*AppDatabase.MIGRATIONS)
            .build()

    @Provides fun blockedDao(db: AppDatabase) = db.blockedApps()
    @Provides fun clipDao(db: AppDatabase) = db.clips()
    @Provides @Singleton fun prefs(@ApplicationContext c: Context): DataStore<Preferences> = c.dataStore
}
