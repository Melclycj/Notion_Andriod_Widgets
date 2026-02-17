package com.notionwidgets.data.repository

import com.notionwidgets.data.local.TaskDao
import com.notionwidgets.data.local.TaskEntity
import com.notionwidgets.data.provider.TaskProvider
import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.model.TaskUpdate
import com.notionwidgets.domain.repository.TaskRepository
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val provider: TaskProvider,
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun getTasks(databaseId: String): Result<List<Task>> {
        return provider.fetchTasks(databaseId)
            .onSuccess { tasks ->
                taskDao.deleteByDatabase(databaseId)
                taskDao.insertAll(tasks.map { TaskEntity.fromDomain(it) })
            }
            .recoverCatching {
                taskDao.getTasksByDatabase(databaseId).map { it.toDomain() }
            }
    }

    override suspend fun addTask(databaseId: String, title: String): Result<Task> {
        return provider.addTask(databaseId, title)
            .onSuccess { task ->
                taskDao.insert(TaskEntity.fromDomain(task))
            }
    }

    override suspend fun updateTask(taskId: String, updates: TaskUpdate): Result<Task> {
        return provider.updateTask(taskId, updates)
            .onSuccess { task ->
                taskDao.insert(TaskEntity.fromDomain(task))
            }
    }

    override suspend fun completeTask(taskId: String): Result<Task> {
        return updateTask(taskId, TaskUpdate(isCompleted = true))
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return provider.deleteTask(taskId)
            .onSuccess {
                taskDao.deleteById(taskId)
            }
    }
}
