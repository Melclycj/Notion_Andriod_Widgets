package com.notionwidgets.domain.usecase

import com.notionwidgets.domain.model.ProviderType
import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UseCaseTest {

    private lateinit var repository: TaskRepository

    private val testTask = Task(
        id = "task-1",
        title = "Test",
        isCompleted = false,
        databaseId = "db-1",
        providerType = ProviderType.NOTION
    )

    @Before
    fun setUp() {
        repository = mockk()
    }

    @Test
    fun `GetTasksUseCase delegates to repository`() = runTest {
        coEvery { repository.getTasks("db-1") } returns Result.success(listOf(testTask))

        val useCase = GetTasksUseCase(repository)
        val result = useCase("db-1")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        coVerify { repository.getTasks("db-1") }
    }

    @Test
    fun `AddTaskUseCase delegates to repository`() = runTest {
        coEvery { repository.addTask("db-1", "New") } returns Result.success(testTask)

        val useCase = AddTaskUseCase(repository)
        val result = useCase("db-1", "New")

        assertTrue(result.isSuccess)
        coVerify { repository.addTask("db-1", "New") }
    }

    @Test
    fun `CompleteTaskUseCase delegates to repository`() = runTest {
        val completed = testTask.copy(isCompleted = true)
        coEvery { repository.completeTask("task-1") } returns Result.success(completed)

        val useCase = CompleteTaskUseCase(repository)
        val result = useCase("task-1")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isCompleted == true)
        coVerify { repository.completeTask("task-1") }
    }

    @Test
    fun `DeleteTaskUseCase delegates to repository`() = runTest {
        coEvery { repository.deleteTask("task-1") } returns Result.success(Unit)

        val useCase = DeleteTaskUseCase(repository)
        val result = useCase("task-1")

        assertTrue(result.isSuccess)
        coVerify { repository.deleteTask("task-1") }
    }

    @Test
    fun `GetTasksUseCase propagates failure`() = runTest {
        coEvery { repository.getTasks("db-1") } returns Result.failure(Exception("fail"))

        val useCase = GetTasksUseCase(repository)
        val result = useCase("db-1")

        assertTrue(result.isFailure)
    }
}
