package com.shohankhan.apexcalculator.feature.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohankhan.apexcalculator.ApexApplication
import com.shohankhan.apexcalculator.core.domain.model.HistoryRecord
import com.shohankhan.apexcalculator.core.domain.model.RetentionPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val query: String = "",
    val records: List<HistoryRecord> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val retention: RetentionPolicy = RetentionPolicy.DEFAULT,
) {
    val isSelecting: Boolean get() = selectedIds.isNotEmpty()

    /** Records grouped under a friendly day heading, newest first. */
    val groups: List<Pair<String, List<HistoryRecord>>>
        get() = records
            .sortedByDescending { it.createdAt }
            .groupBy { HistoryGroups.label(it.createdAt) }
            .toList()
}

/** Local formatting of record timestamps; the history view never talks to the network. */
object HistoryGroups {
    fun label(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val zone = java.time.ZoneId.systemDefault()
        val date = java.time.Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
        val today = java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            else -> java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy").format(date)
        }
    }

    fun time(timestamp: Long): String =
        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochMilli(timestamp))
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ApexApplication).container
    private val query = MutableStateFlow("")
    private val selected = MutableStateFlow<Set<Long>>(emptySet())

    private val records = query.flatMapLatest { text ->
        if (text.isBlank()) container.historyRepository.observe()
        else container.historyRepository.search(text)
    }

    val state: StateFlow<HistoryUiState> = combine(
        records,
        query,
        selected,
        container.settingsRepository.settings,
    ) { records, query, selected, settings ->
        HistoryUiState(
            query = query,
            records = records,
            selectedIds = selected,
            retention = settings.historyRetention,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onQueryChange(value: String) {
        query.value = value
        selected.value = emptySet()
    }

    fun toggleSelection(record: HistoryRecord) {
        selected.value = if (record.id in selected.value) selected.value - record.id else selected.value + record.id
    }

    fun clearSelection() {
        selected.value = emptySet()
    }

    fun deleteSelected() {
        val targets = state.value.records.filter { it.id in selected.value }
        selected.value = emptySet()
        viewModelScope.launch { runCatching { container.historyRepository.deleteMany(targets) } }
    }

    fun delete(record: HistoryRecord) {
        viewModelScope.launch { runCatching { container.historyRepository.delete(record) } }
    }

    fun clearAll() {
        selected.value = emptySet()
        viewModelScope.launch { runCatching { container.historyRepository.clearAll() } }
    }

    /** Re-inserts rows (used by undo affordances and tests). */
    fun restore(records: List<HistoryRecord>) {
        viewModelScope.launch { runCatching { container.historyRepository.restore(records) } }
    }

    /** Applies the retention policy immediately, for the settings action. */
    fun purgeNow() {
        viewModelScope.launch {
            runCatching {
                val settings = container.settingsRepository.settings.first()
                container.historyRepository.purgeExpired(settings.historyRetention)
            }
        }
    }
}
