package com.notionwidgets.data.repository

import com.notionwidgets.data.local.TaskDao
import com.notionwidgets.data.local.TaskEntity
import com.notionwidgets.data.provider.NotionDatabase
import com.notionwidgets.data.provider.TaskProvider
import com.notionwidgets.domain.model.ProviderType
import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.model.TaskUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskRepositoryImplTest {

    private lateinit var provider: TaskProvider
    private lateinit var taskDao: TaskDao
    private lateinit var repository: TaskRepositoryImpl

    private val testTask = Task(
        id = "task-1",
        title = "Test task",
        isCompleted = false,
        createdAt = 1000L,
        updatedAt = 2000L,
        databaseId = "db-1",
        providerType = ProviderType.NOTION
    )

    @Before
    fun setUp() {
        provider = mockk()
        taskDao = mockk(relaxUnitFun = true)
        repository = TaskRepositoryImpl(provider, taskDao)
    }

    @Test
    fun `getTasks returns remote tasks and caches them`() = runTest {
        val tasks = listOf(testTask)
        coEvery { provider.fetchTasks("db-1") } returns Result.success(tasks)

        val result = repository.getTasks("db-1")

        assertTrue(result.isSuccess)
        assertEquals(tasks, result.getOrNull())
        coVerify { taskDao.deleteByDatabase("db-1") }
        coVerify { taskDao.insertAll(any()) }
    }

    @Test
    fun `getTasks falls back to cache on remote failure`() = runTest {
        val cachedEntity = TaskEntity(
            id = "task-1",
            title = "Cached task",
            isCompleted = false,
            createdAt = 1000L,
            updatedAt = 2000L,
            databaseId = "db-1",
            providerType = "NOTION"
        )
        coEvery { provider.fetchTasks("db-1") } returns Result.failure(Exception("Network error"))
        coEvery { taskDao.getTasksByDatabase("db-1") } returns listOf(cachedEntity)

        val result = repository.getTasks("db-1")

        assertTrue(result.isSuccess)
        assertEquals("Cached task", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `addTask calls provider and caches result`() = runTest {
        coEvery { provider.addTask("db-1", "New task") } returns Result.success(testTask)

        val result = repository.addTask("db-1", "New task")

        assertTrue(result.isSuccess)
        assertEquals(testTask, result.getOrNull())
        coVerify { taskDao.insert(any()) }
    }

    @Test
    fun `addTask does not cache on failure`() = runTest {
        coEvery { provider.addTask("db-1", "Fail") } returns Result.failure(Exception("API error"))

        val result = repository.addTask("db-1", "Fail")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { taskDao.insert(any()) }
    }

    @Test
    fun `updateTask calls provider and caches result`() = runTest {
        val updated = testTask.copy(title = "Updated")
        val updates = TaskUpdate(title = "Updated")
        coEvery { provider.updateTask("task-1", updates) } returns Result.success(updated)

        val result = repository.updateTask("task-1", updates)

        assertTrue(result.isSuccess)
        assertEquals("Updated", result.getOrNull()?.title)
        coVerify { taskDao.insert(any()) }
    }

    @Test
    fun `completeTask delegates to updateTask with isCompleted true`() = runTest {
        val completed = testTask.copy(isCompleted = true)
        val expectedUpdate = TaskUpdate(isCompleted = true)
        coEvery { provider.updateTask("task-1", expectedUpdate) } returns Result.success(completed)

        val result = repository.completeTask("task-1")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isCompleted == true)
    }

    @Test
    fun `deleteTask calls provider and removes from cache`() = runTest {
        coEvery { provider.deleteTask("task-1") } returns Result.success(Unit)

        val result = repository.deleteTask("task-1")

        assertTrue(result.isSuccess)
        coVerify { taskDao.deleteById("task-1") }
    }

    @Test
    fun `deleteTask does not remove from cache on failure`() = runTest {
        coEvery { provider.deleteTask("task-1") } returns Result.failure(Exception("API error"))

        val result = repository.deleteTask("task-1")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { taskDao.deleteById(any()) }
    }
}
