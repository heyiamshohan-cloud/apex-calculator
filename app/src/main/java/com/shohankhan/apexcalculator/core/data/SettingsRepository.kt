package com.shohankhan.apexcalculator.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shohankhan.apexcalculator.core.domain.model.AccentChoice
import com.shohankhan.apexcalculator.core.domain.model.AngleMode
import com.shohankhan.apexcalculator.core.domain.model.AppSettings
import com.shohankhan.apexcalculator.core.domain.model.NumberFormatMode
import com.shohankhan.apexcalculator.core.domain.model.RetentionPolicy
import com.shohankhan.apexcalculator.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "apex_settings")

/** Local-only preference storage backed by DataStore. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val accent = stringPreferencesKey("accent")
        val angle = stringPreferencesKey("angle")
        val numberFormat = stringPreferencesKey("number_format")
        val decimalPlaces = intPreferencesKey("decimal_places")
        val significantFigures = intPreferencesKey("significant_figures")
        val thousands = booleanPreferencesKey("thousands")
        val fraction = booleanPreferencesKey("fraction")
        val sciThreshold = intPreferencesKey("sci_threshold")
        val defaultTab = intPreferencesKey("default_tab")
        val haptics = booleanPreferencesKey("haptics")
        val reducedMotion = booleanPreferencesKey("reduced_motion")
        val retentionDays = intPreferencesKey("retention_days")
        val persistMemory = booleanPreferencesKey("persist_memory")
        val autoSaveHistory = booleanPreferencesKey("auto_save_history")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { it.toSettings() }
        .distinctUntilChanged()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { preferences ->
            val updated = transform(preferences.toSettings())
            preferences[Keys.theme] = updated.themeMode.name
            preferences[Keys.accent] = updated.accent.name
            preferences[Keys.angle] = updated.angleMode.name
            preferences[Keys.numberFormat] = updated.numberFormat.name
            preferences[Keys.decimalPlaces] = updated.decimalPlaces
            preferences[Keys.significantFigures] = updated.significantFigures
            preferences[Keys.thousands] = updated.thousandsSeparator
            preferences[Keys.fraction] = updated.fractionDisplay
            preferences[Keys.sciThreshold] = updated.scientificThreshold
            preferences[Keys.defaultTab] = updated.defaultTabIndex
            preferences[Keys.haptics] = updated.hapticsEnabled
            preferences[Keys.reducedMotion] = updated.reducedMotion
            preferences[Keys.retentionDays] = updated.historyRetention.days
            preferences[Keys.persistMemory] = updated.persistMemory
            preferences[Keys.autoSaveHistory] = updated.autoSaveHistory
        }
    }

    suspend fun reset() {
        context.settingsDataStore.edit { it.clear() }
    }

    private fun Preferences.toSettings(): AppSettings = AppSettings(
        themeMode = enumOr(this[Keys.theme], ThemeMode.SYSTEM),
        accent = enumOr(this[Keys.accent], AccentChoice.OCEAN),
        angleMode = enumOr(this[Keys.angle], AngleMode.DEG),
        numberFormat = enumOr(this[Keys.numberFormat], NumberFormatMode.AUTO),
        decimalPlaces = (this[Keys.decimalPlaces] ?: 10).coerceIn(0, 15),
        significantFigures = (this[Keys.significantFigures] ?: 10).coerceIn(3, 15),
        thousandsSeparator = this[Keys.thousands] ?: true,
        fractionDisplay = this[Keys.fraction] ?: false,
        scientificThreshold = (this[Keys.sciThreshold] ?: 12).coerceIn(6, 18),
        defaultTabIndex = (this[Keys.defaultTab] ?: 0).coerceIn(0, 5),
        hapticsEnabled = this[Keys.haptics] ?: true,
        reducedMotion = this[Keys.reducedMotion] ?: false,
        historyRetention = RetentionPolicy.fromDays(this[Keys.retentionDays] ?: RetentionPolicy.DEFAULT.days),
        persistMemory = this[Keys.persistMemory] ?: true,
        autoSaveHistory = this[Keys.autoSaveHistory] ?: true,
    )

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        if (name == null) fallback else runCatching { enumValueOf<T>(name) }.getOrDefault(fallback)
}
