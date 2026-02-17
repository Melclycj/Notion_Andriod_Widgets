package com.notionwidgets.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.model.TaskUpdate
import com.notionwidgets.domain.repository.TaskRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class TodoGlanceWidget : GlanceAppWidget() {

    companion object {
        val taskIdKey = ActionParameters.Key<String>("task_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.taskRepository()
        val authManager = entryPoint.authManager()

        val databaseId = authManager.getSelectedDatabaseId()

        val tasks: List<Task>
        val error: String?

        if (databaseId == null) {
            tasks = emptyList()
            error = "No database selected"
        } else {
            val result = repository.getTasks(databaseId)
            tasks = result.getOrDefault(emptyList())
            error = result.exceptionOrNull()?.message
        }

        provideContent {
            GlanceTheme {
                TodoWidgetContent(
                    tasks = tasks,
                    isLoading = false,
                    error = error
                )
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        TodoGlanceWidget().update(context, glanceId)
    }
}

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TodoGlanceWidget.taskIdKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.taskRepository()

        // Toggle: we don't know current state, so we complete it
        // A more robust approach would check current state first
        repository.updateTask(taskId, TaskUpdate(isCompleted = true))
        TodoGlanceWidget().update(context, glanceId)
    }
}

class AddTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetEntryPoint::class.java
        )
        val authManager = entryPoint.authManager()
        val repository = entryPoint.taskRepository()

        val databaseId = authManager.getSelectedDatabaseId() ?: return
        repository.addTask(databaseId, "New task")
        TodoGlanceWidget().update(context, glanceId)
    }
}

class DeleteTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TodoGlanceWidget.taskIdKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.taskRepository()

        repository.deleteTask(taskId)
        TodoGlanceWidget().update(context, glanceId)
    }
}
