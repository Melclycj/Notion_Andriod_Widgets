package com.notionwidgets.data.provider

import com.notionwidgets.domain.model.ProviderType
import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.model.TaskUpdate

interface TaskProvider {
    val providerType: ProviderType
    suspend fun fetchTasks(databaseId: String): Result<List<Task>>
    suspend fun addTask(databaseId: String, title: String): Result<Task>
    suspend fun updateTask(taskId: String, updates: TaskUpdate): Result<Task>
    suspend fun deleteTask(taskId: String): Result<Unit>
    suspend fun searchDatabases(): Result<List<NotionDatabase>>
}

data class NotionDatabase(
    val id: String,
    val title: String
)
