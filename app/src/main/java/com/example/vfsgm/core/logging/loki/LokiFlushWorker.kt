package com.example.vfsgm.core.logging.loki

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.vfsgm.core.logging.LogManager
import java.util.concurrent.TimeUnit

class LokiFlushWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val buffer = LogManager.lokiLogBuffer ?: return Result.success()
        val client = LogManager.lokiClient ?: return Result.success()

        repeat(5) {
            val batch = buffer.dequeueBatch()
            if (batch.isEmpty()) return Result.success()

            when (val uploadResult = client.push(batch)) {
                LokiUploadResult.Success -> Unit
                is LokiUploadResult.RetryableFailure -> {
                    println(
                        "Loki flush retryable failure: code=${uploadResult.code} reason=${uploadResult.reason}"
                    )
                    buffer.requeueFront(batch)
                    return Result.retry()
                }
                is LokiUploadResult.FatalFailure -> {
                    println(
                        "Loki flush fatal failure: code=${uploadResult.code} reason=${uploadResult.reason}"
                    )
                }
            }
        }

        return Result.success()
    }

    companion object {
        private const val UNIQUE_PERIODIC_WORK = "loki-flush-periodic"
        private const val UNIQUE_NORMAL_FLUSH_WORK = "loki-flush-normal"
        private const val UNIQUE_URGENT_FLUSH_WORK = "loki-flush-urgent"
        private const val UNIQUE_DELAYED_FLUSH_WORK = "loki-flush-delayed"

        private fun networkConstraints() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<LokiFlushWorker>(15, TimeUnit.MINUTES)
                .setConstraints(networkConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun scheduleImmediate(context: Context, urgent: Boolean) {
            val requestBuilder = OneTimeWorkRequestBuilder<LokiFlushWorker>()
                .setConstraints(networkConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)

            if (urgent) {
                requestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }

            val workName = if (urgent) UNIQUE_URGENT_FLUSH_WORK else UNIQUE_NORMAL_FLUSH_WORK
            val policy = if (urgent) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP

            WorkManager.getInstance(context)
                .enqueueUniqueWork(workName, policy, requestBuilder.build())
        }

        fun scheduleDelayed(context: Context, delayMs: Long = LokiConfig.AGE_TRIGGER_MS) {
            val request = OneTimeWorkRequestBuilder<LokiFlushWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setConstraints(networkConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_DELAYED_FLUSH_WORK, ExistingWorkPolicy.KEEP, request)
        }
    }
}
