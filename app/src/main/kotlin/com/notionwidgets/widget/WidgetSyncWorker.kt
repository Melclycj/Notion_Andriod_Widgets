package com.notionwidgets.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TaskRepository,
    private val authManager: AuthManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val databaseId = authManager.getSelectedDatabaseId() ?: return Result.success()

        repository.getTasks(databaseId)
            .onSuccess {
                TodoGlanceWidget().updateAll(applicationContext)
            }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_sync"

        fun enqueue(context: Context, intervalMinutes: Long = 15L) {
            val interval = intervalMinutes.coerceAtLeast(15L)
            val request = PeriodicWorkRequestBuilder<WidgetSyncWorker>(
                interval, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
