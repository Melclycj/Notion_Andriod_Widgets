package com.notionwidgets.domain.repository

import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.model.TaskUpdate

interface TaskRepository {
    suspend fun getTasks(databaseId: String): Result<List<Task>>
    suspend fun addTask(databaseId: String, title: String): Result<Task>
    suspend fun updateTask(taskId: String, updates: TaskUpdate): Result<Task>
    suspend fun completeTask(taskId: String): Result<Task>
    suspend fun deleteTask(taskId: String): Result<Unit>
}
