package com.shohankhan.apexcalculator.core.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shohankhan.apexcalculator.ApexApplication
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Exposes the persisted settings to secondary screens that need angle mode or formatting. */
class SettingsStateViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ApexApplication).container

    val settings: StateFlow<AppSettings> = container.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())
}
