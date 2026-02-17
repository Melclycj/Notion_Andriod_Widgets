package com.notionwidgets.ui.database

import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.data.provider.NotionDatabase
import com.notionwidgets.data.provider.TaskProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseSelectViewModelTest {

    private lateinit var taskProvider: TaskProvider
    private lateinit var authManager: AuthManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        taskProvider = mockk()
        authManager = mockk(relaxed = true)
        every { authManager.getSelectedDatabaseId() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads databases successfully`() = runTest {
        val databases = listOf(
            NotionDatabase("db-1", "Work Tasks"),
            NotionDatabase("db-2", "Personal")
        )
        coEvery { taskProvider.searchDatabases() } returns Result.success(databases)

        val viewModel = DatabaseSelectViewModel(taskProvider, authManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.databases.size)
        assertEquals("Work Tasks", state.databases[0].title)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `init shows error on failure`() = runTest {
        coEvery { taskProvider.searchDatabases() } returns Result.failure(Exception("Network error"))

        val viewModel = DatabaseSelectViewModel(taskProvider, authManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.databases.isEmpty())
        assertEquals("Network error", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `init restores previously selected database`() = runTest {
        every { authManager.getSelectedDatabaseId() } returns "db-2"
        coEvery { taskProvider.searchDatabases() } returns Result.success(
            listOf(NotionDatabase("db-1", "A"), NotionDatabase("db-2", "B"))
        )

        val viewModel = DatabaseSelectViewModel(taskProvider, authManager)
        advanceUntilIdle()

        assertEquals("db-2", viewModel.uiState.value.selectedId)
    }

    @Test
    fun `selectDatabase saves and updates state`() = runTest {
        coEvery { taskProvider.searchDatabases() } returns Result.success(emptyList())

        val viewModel = DatabaseSelectViewModel(taskProvider, authManager)
        advanceUntilIdle()

        viewModel.selectDatabase("db-42")

        assertEquals("db-42", viewModel.uiState.value.selectedId)
        verify { authManager.saveSelectedDatabaseId("db-42") }
    }

    @Test
    fun `loadDatabases can be called to refresh`() = runTest {
        coEvery { taskProvider.searchDatabases() } returns Result.failure(Exception("fail"))

        val viewModel = DatabaseSelectViewModel(taskProvider, authManager)
        advanceUntilIdle()
        assertEquals("fail", viewModel.uiState.value.error)

        coEvery { taskProvider.searchDatabases() } returns Result.success(
            listOf(NotionDatabase("db-1", "Recovered"))
        )
        viewModel.loadDatabases()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.databases.size)
        assertNull(viewModel.uiState.value.error)
    }
}
