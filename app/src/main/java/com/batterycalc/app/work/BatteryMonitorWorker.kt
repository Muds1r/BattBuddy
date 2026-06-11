package com.batterycalc.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.batterycalc.app.BatteryCalcApp
import java.util.concurrent.TimeUnit

class BatteryMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as BatteryCalcApp
        app.repository.syncChargingState(applicationContext)

        if (app.repository.hasActiveSession()) {
            scheduleSoonCheck(applicationContext)
        }
        return Result.success()
    }

    companion object {
        private const val PERIODIC_NAME = "battery_monitor_periodic"
        private const val SOON_NAME = "battery_monitor_soon"
        private const val SOON_DELAY_MINUTES = 5L
        private const val PERIODIC_INTERVAL_MINUTES = 15L

        fun schedule(context: Context) {
            val periodic = PeriodicWorkRequestBuilder<BatteryMonitorWorker>(
                PERIODIC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodic
            )

            scheduleImmediateCheck(context)
        }

        fun scheduleImmediateCheck(context: Context) {
            val request = OneTimeWorkRequestBuilder<BatteryMonitorWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                SOON_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        private fun scheduleSoonCheck(context: Context) {
            val request = OneTimeWorkRequestBuilder<BatteryMonitorWorker>()
                .setInitialDelay(SOON_DELAY_MINUTES, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                SOON_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
