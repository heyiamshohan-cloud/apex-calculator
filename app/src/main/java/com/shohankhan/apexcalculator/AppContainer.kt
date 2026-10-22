package com.shohankhan.apexcalculator

import android.content.Context
import com.shohankhan.apexcalculator.core.data.ApexDatabase
import com.shohankhan.apexcalculator.core.data.FavoritesRepository
import com.shohankhan.apexcalculator.core.data.HistoryCleanupWorker
import com.shohankhan.apexcalculator.core.data.HistoryRepository
import com.shohankhan.apexcalculator.core.data.MemoryRepository
import com.shohankhan.apexcalculator.core.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manual dependency container. One instance lives in [ApexApplication] for the whole process.
 */
class AppContainer(private val context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: ApexDatabase by lazy { ApexDatabase.get(context) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(context) }

    val historyRepository: HistoryRepository by lazy { HistoryRepository(database.historyDao()) }

    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepository(database.favoriteDao()) }

    val memoryRepository: MemoryRepository by lazy {
        MemoryRepository(context, settingsRepository, applicationScope)
    }

    /** Runs on every cold start so expired records never remain visible. */
    fun purgeExpiredHistory() {
        applicationScope.launch {
            runCatching {
                val settings = settingsRepository.settings.first()
                historyRepository.purgeExpired(settings.historyRetention)
            }
        }
    }

    fun scheduleBackgroundCleanup() {
        HistoryCleanupWorker.schedule(context)
    }

    fun warmUp() {
        applicationScope.launch {
            withContext(Dispatchers.IO) {
                runCatching { database.historyDao() }
                runCatching { settingsRepository.settings.first() }
            }
        }
    }
}
