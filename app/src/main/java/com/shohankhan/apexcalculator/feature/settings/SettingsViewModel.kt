package com.shohankhan.apexcalculator.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohankhan.apexcalculator.ApexApplication
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ApexApplication).container

    val settings: StateFlow<AppSettings> = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { runCatching { container.settingsRepository.update(transform) } }
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun clearHistory() {
        viewModelScope.launch {
            runCatching {
                container.historyRepository.clearAll()
                _message.value = "Calculation history deleted"
            }
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            runCatching {
                container.favoritesRepository.clear()
                _message.value = "Favorites deleted"
            }
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            runCatching {
                container.settingsRepository.update { AppSettings() }
                _message.value = "Settings restored to defaults"
            }
        }
    }
}
