package com.shohankhan.apexcalculator.core.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val expression: String,
    val result: String,
    val source: String,
    val createdAt: Long,
    val details: String? = null,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val key: String,
    val kind: String,
    val refId: String,
    val title: String,
    val subtitle: String,
    val savedAt: Long,
)

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query(
        "SELECT * FROM history WHERE expression LIKE '%' || :query || '%' " +
            "OR result LIKE '%' || :query || '%' ORDER BY createdAt DESC",
    )
    fun search(query: String): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(entity: HistoryEntity): Long

    @Delete
    suspend fun delete(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM history")
    suspend fun clear()

    @Query("DELETE FROM history WHERE createdAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long): Int

    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun findById(id: Long): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun restore(entities: List<HistoryEntity>)
}

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE key = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT * FROM favorites WHERE key = :key LIMIT 1")
    suspend fun findByKey(key: String): FavoriteEntity?

    @Query("DELETE FROM favorites")
    suspend fun clear()
}

@Database(
    entities = [HistoryEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ApexDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        private const val NAME = "apex_calculator.db"

        @Volatile
        private var instance: ApexDatabase? = null

        fun get(context: Context): ApexDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                ApexDatabase::class.java,
                NAME,
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
