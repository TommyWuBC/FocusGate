package com.focusgate.data

import androidx.room.*
import androidx.room.migration.Migration
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(@PrimaryKey val packageName: String, val label: String)

@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val title: String,
)

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY label") fun observe(): Flow<List<BlockedAppEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: BlockedAppEntity)
    @Query("DELETE FROM blocked_apps WHERE packageName = :pkg") suspend fun delete(pkg: String)
}

@Dao
interface ClipDao {
    @Query("SELECT * FROM clips ORDER BY id") fun observe(): Flow<List<ClipEntity>>
    @Insert suspend fun insert(e: ClipEntity): Long
    @Query("DELETE FROM clips WHERE id = :id") suspend fun delete(id: Long)
}

/** Schema is exported to core/data/schemas. Bump [version] and add a Migration for every change; never use destructive fallback. */
@Database(entities = [BlockedAppEntity::class, ClipEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedApps(): BlockedAppDao
    abstract fun clips(): ClipDao

    companion object {
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
