package com.shohankhan.apexcalculator.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shohankhan.apexcalculator.core.domain.model.CalculationSource
import com.shohankhan.apexcalculator.core.domain.model.FavoriteItem
import com.shohankhan.apexcalculator.core.domain.model.FavoriteKind
import com.shohankhan.apexcalculator.core.domain.model.HistoryRecord
import com.shohankhan.apexcalculator.core.domain.model.MemoryState
import com.shohankhan.apexcalculator.core.domain.model.RetentionPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HistoryRepository(private val dao: HistoryDao) {

    fun observe(): Flow<List<HistoryRecord>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun search(query: String): Flow<List<HistoryRecord>> =
        dao.search(query.trim()).map { entities -> entities.map { it.toDomain() } }

    suspend fun add(record: HistoryRecord): Long = dao.insert(record.toEntity())

    suspend fun delete(record: HistoryRecord) = dao.delete(record.toEntity())

    suspend fun deleteMany(records: List<HistoryRecord>) = dao.deleteByIds(records.map { it.id })

    suspend fun clearAll() = dao.clear()

    suspend fun restore(records: List<HistoryRecord>) = dao.restore(records.map { it.toEntity() })

    /**
     * Deletes records older than the retention window. A policy of NEVER keeps everything.
     * Returns the number of deleted rows so callers can report the outcome.
     */
    suspend fun purgeExpired(policy: RetentionPolicy, now: Long = System.currentTimeMillis()): Int {
        if (policy == RetentionPolicy.NEVER) return 0
        val cutoff = now - policy.days.toLong() * 24L * 60L * 60L * 1000L
        return dao.deleteOlderThan(cutoff)
    }

    private fun HistoryEntity.toDomain() = HistoryRecord(
        id = id,
        expression = expression,
        result = result,
        source = runCatching { CalculationSource.valueOf(source) }.getOrDefault(CalculationSource.STANDARD),
        createdAt = createdAt,
        details = details,
    )

    private fun HistoryRecord.toEntity() = HistoryEntity(
        id = id,
        expression = expression,
        result = result,
        source = source.name,
        createdAt = createdAt,
        details = details,
    )
}

class FavoritesRepository(private val dao: FavoriteDao) {

    fun observe(): Flow<List<FavoriteItem>> = dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeKind(kind: FavoriteKind): Flow<List<FavoriteItem>> =
        observe().map { items -> items.filter { it.kind == kind } }

    suspend fun toggle(item: FavoriteItem): Boolean {
        val existing = dao.findByKey(item.key)
        return if (existing == null) {
            dao.insert(item.toEntity())
            true
        } else {
            dao.deleteByKey(item.key)
            false
        }
    }

    suspend fun remove(item: FavoriteItem) = dao.deleteByKey(item.key)

    suspend fun clear() = dao.clear()

    private fun FavoriteEntity.toDomain() = FavoriteItem(
        kind = runCatching { FavoriteKind.valueOf(kind) }.getOrDefault(FavoriteKind.FORMULA),
        refId = refId,
        title = title,
        subtitle = subtitle,
        savedAt = savedAt,
    )

    private fun FavoriteItem.toEntity() = FavoriteEntity(
        key = key,
        kind = kind.name,
        refId = refId,
        title = title,
        subtitle = subtitle,
        savedAt = savedAt,
    )
}

private val Context.stateDataStore: DataStore<Preferences> by preferencesDataStore(name = "apex_state")

/**
 * Memory register and last-answer storage. Values are stored as plain expression text so exact
 * fractions and large integers survive a round trip.
 */
class MemoryRepository(
    context: Context,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
) {

    private object Keys {
        val memory = stringPreferencesKey("memory")
        val lastAnswer = stringPreferencesKey("last_answer")
    }

    private val store = context.stateDataStore
    private val _memory = MutableStateFlow(MemoryState.EMPTY)
    val memory: StateFlow<MemoryState> = _memory.asStateFlow()

    private val _lastAnswer = MutableStateFlow<String?>(null)
    val lastAnswer: StateFlow<String?> = _lastAnswer.asStateFlow()

    init {
        scope.launch {
            runCatching {
                val settings = settingsRepository.settings.first()
                val preferences = store.data.catch { emit(emptyPreferences()) }.first()
                if (settings.persistMemory) {
                    val text = preferences[Keys.memory]
                    _memory.value = MemoryState(text)
                }
                _lastAnswer.value = preferences[Keys.lastAnswer]
            }
        }
    }

    fun setMemory(text: String?) {
        _memory.value = MemoryState(text)
        scope.launch {
            runCatching {
                store.edit { preferences ->
                    if (text.isNullOrBlank()) preferences.remove(Keys.memory) else preferences[Keys.memory] = text
                }
            }
        }
    }

    fun clearMemory() {
        _memory.value = MemoryState.EMPTY
        scope.launch {
            runCatching { store.edit { it.remove(Keys.memory) } }
        }
    }

    fun rememberAnswer(text: String?) {
        _lastAnswer.value = text
        scope.launch {
            runCatching {
                store.edit { preferences ->
                    if (text.isNullOrBlank()) preferences.remove(Keys.lastAnswer) else preferences[Keys.lastAnswer] = text
                }
            }
        }
    }

    suspend fun currentSettings() = settingsRepository.settings.first()

    /** Applies a memory operation using parsed values supplied by the caller. */
    fun update(transform: (MemoryState) -> MemoryState) {
        val updated = transform(_memory.value)
        setMemory(updated.text)
    }

    companion object {
        /** Returns true when the text represents a usable memory value. */
        fun isUsable(state: MemoryState): Boolean = state.isSet && !state.text.isNullOrBlank()
    }
}
