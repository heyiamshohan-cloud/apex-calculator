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

    val groups: List<Pair<String, List<HistoryRecord>>> get() =
        records.groupBy { HistoryGroups.label(it.createdAt) }
            .toList()
            .sortedByDescending { (_, items) -> items.maxOf { it.createdAt } }
}

/** Groups records by calendar day using the device time zone. */
object HistoryGroups {
    fun label(timestamp: Long): String {
        val zone = java.time.ZoneId.systemDefault()
        val date = java.time.Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
        val today = java.time.LocalDate.now(zone)
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
    private val selection = MutableStateFlow<Set<Long>>(emptySet())

    private val records = query.flatMapLatest { text ->
        if (text.isBlank()) container.historyRepository.observe()
        else container.historyRepository.search(text)
    }

    val state: StateFlow<HistoryUiState> = combine(
        records,
        query,
        selection,
        container.settingsRepository.settings,
    ) { records, query, selection, settings ->
        HistoryUiState(
            query = query,
            records = records,
            selectedIds = selection.filter { id -> records.any { it.id == id } }.toSet(),
            retention = settings.historyRetention,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun toggleSelection(record: HistoryRecord) {
        selection.value = if (record.id in selection.value) {
            selection.value - record.id
        } else {
            selection.value + record.id
        }
    }

    fun clearSelection() {
        selection.value = emptySet()
    }

    fun deleteSelected() {
        val ids = selection.value
        val targets = state.value.records.filter { it.id in ids }
        selection.value = emptySet()
        viewModelScope.launch { runCatching { container.historyRepository.deleteMany(targets) } }
    }

    fun delete(record: HistoryRecord) {
        viewModelScope.launch { runCatching { container.historyRepository.delete(record) } }
    }

    fun clearAll() {
        selection.value = emptySet()
        viewModelScope.launch { runCatching { container.historyRepository.clearAll() } }
    }

    /** Undo support: re-inserts the deleted rows with their original timestamps. */
    fun restore(records: List<HistoryRecord>) {
        viewModelScope.launch { runCatching { container.historyRepository.restore(records) } }
    }

    fun purgeNow() {
        viewModelScope.launch {
            runCatching {
                val settings = container.settingsRepository.settings.first()
                container.historyRepository.purgeExpired(settings.historyRetention)
            }
        }
    }
}
