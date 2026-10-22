package com.shohankhan.apexcalculator.core.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shohankhan.apexcalculator.ApexApplication
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Opportunistic background cleanup for expired history records. Android does not guarantee an
 * exact execution time for periodic work, so the same purge also runs whenever the application
 * starts and whenever the history screen is opened.
 */
class HistoryCleanupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? ApexApplication ?: return Result.success()
        return try {
            val settings = application.container.settingsRepository.settings.first()
            val removed = application.container.historyRepository.purgeExpired(settings.historyRetention)
            Log.i(TAG, "Scheduled history cleanup removed $removed expired record(s)")
            Result.success()
        } catch (exception: Exception) {
            Log.w(TAG, "History cleanup failed", exception)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HistoryCleanup"
        private const val WORK_NAME = "apex-history-cleanup"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<HistoryCleanupWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(6, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
