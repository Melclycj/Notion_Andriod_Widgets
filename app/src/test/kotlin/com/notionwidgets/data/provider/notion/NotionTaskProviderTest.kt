package com.notionwidgets.data.provider.notion

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.notionwidgets.domain.model.ProviderType
import com.notionwidgets.domain.model.TaskUpdate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotionTaskProviderTest {

    private lateinit var api: NotionApiService
    private lateinit var mapper: NotionMapper
    private lateinit var provider: NotionTaskProvider

    private fun buildPage(
        id: String,
        title: String = "Test",
        checked: Boolean = false,
        archived: Boolean = false
    ): NotionPage {
        val titleProp = JsonObject().apply {
            addProperty("type", "title")
            add("title", JsonArray().apply {
                add(JsonObject().apply { addProperty("plain_text", title) })
            })
        }
        val checkProp = JsonObject().apply {
            addProperty("type", "checkbox")
            addProperty("checkbox", checked)
        }
        return NotionPage(
            id = id,
            properties = mapOf("Name" to titleProp, "Done" to checkProp),
            createdTime = "2025-01-01T00:00:00.000Z",
            lastEditedTime = "2025-01-01T00:00:00.000Z",
            archived = archived
        )
    }

    @Before
    fun setUp() {
        api = mockk()
        mapper = NotionMapper()
        provider = NotionTaskProvider(api, mapper)
    }

    @Test
    fun `providerType is NOTION`() {
        assertEquals(ProviderType.NOTION, provider.providerType)
    }

    // --- fetchTasks ---

    @Test
    fun `fetchTasks returns mapped tasks`() = runTest {
        val page1 = buildPage("p1", "Task 1")
        val page2 = buildPage("p2", "Task 2", checked = true)
        coEvery { api.queryDatabase("db-1", any()) } returns NotionQueryResponse(
            results = listOf(page1, page2)
        )

        val result = provider.fetchTasks("db-1")

        assertTrue(result.isSuccess)
        val tasks = result.getOrNull()!!
        assertEquals(2, tasks.size)
        assertEquals("Task 1", tasks[0].title)
        assertEquals(false, tasks[0].isCompleted)
        assertEquals("Task 2", tasks[1].title)
        assertEquals(true, tasks[1].isCompleted)
    }

    @Test
    fun `fetchTasks filters out archived pages`() = runTest {
        val active = buildPage("p1", "Active")
        val archived = buildPage("p2", "Archived", archived = true)
        coEvery { api.queryDatabase("db-1", any()) } returns NotionQueryResponse(
            results = listOf(active, archived)
        )

        val result = provider.fetchTasks("db-1")

        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Active", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `fetchTasks returns empty list for empty database`() = runTest {
        coEvery { api.queryDatabase("db-1", any()) } returns NotionQueryResponse(results = emptyList())

        val result = provider.fetchTasks("db-1")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun `fetchTasks returns failure on API error`() = runTest {
        coEvery { api.queryDatabase(any(), any()) } throws RuntimeException("Network error")

        val result = provider.fetchTasks("db-1")

        assertTrue(result.isFailure)
    }

    // --- addTask ---

    @Test
    fun `addTask sends correct body and returns mapped task`() = runTest {
        val bodySlot = slot<JsonObject>()
        val createdPage = buildPage("new-1", "Buy milk")
        coEvery { api.createPage(capture(bodySlot)) } returns createdPage

        val result = provider.addTask("db-1", "Buy milk")

        assertTrue(result.isSuccess)
        assertEquals("Buy milk", result.getOrNull()?.title)

        val body = bodySlot.captured
        assertEquals("db-1", body.getAsJsonObject("parent").get("database_id").asString)
        val titleText = body.getAsJsonObject("properties")
            .getAsJsonObject("Name")
            .getAsJsonArray("title")
            .get(0).asJsonObject
            .getAsJsonObject("text")
            .get("content").asString
        assertEquals("Buy milk", titleText)
    }

    @Test
    fun `addTask returns failure on API error`() = runTest {
        coEvery { api.createPage(any()) } throws RuntimeException("API error")

        val result = provider.addTask("db-1", "Fail")

        assertTrue(result.isFailure)
    }

    // --- updateTask ---

    @Test
    fun `updateTask sends title update`() = runTest {
        val bodySlot = slot<JsonObject>()
        val updatedPage = buildPage("p1", "Updated title")
        coEvery { api.updatePage(eq("p1"), capture(bodySlot)) } returns updatedPage

        val result = provider.updateTask("p1", TaskUpdate(title = "Updated title"))

        assertTrue(result.isSuccess)
        val props = bodySlot.captured.getAsJsonObject("properties")
        assertTrue(props.has("Name"))
    }

    @Test
    fun `updateTask sends checkbox update`() = runTest {
        val bodySlot = slot<JsonObject>()
        val updatedPage = buildPage("p1", checked = true)
        coEvery { api.updatePage(eq("p1"), capture(bodySlot)) } returns updatedPage

        val result = provider.updateTask("p1", TaskUpdate(isCompleted = true))

        assertTrue(result.isSuccess)
        val props = bodySlot.captured.getAsJsonObject("properties")
        assertTrue(props.has("Done"))
        assertTrue(props.getAsJsonObject("Done").get("checkbox").asBoolean)
    }

    @Test
    fun `updateTask sends both title and checkbox`() = runTest {
        val bodySlot = slot<JsonObject>()
        val updatedPage = buildPage("p1", "New", checked = true)
        coEvery { api.updatePage(eq("p1"), capture(bodySlot)) } returns updatedPage

        val result = provider.updateTask("p1", TaskUpdate(title = "New", isCompleted = true))

        assertTrue(result.isSuccess)
        val props = bodySlot.captured.getAsJsonObject("properties")
        assertTrue(props.has("Name"))
        assertTrue(props.has("Done"))
    }

    @Test
    fun `updateTask returns failure on API error`() = runTest {
        coEvery { api.updatePage(any(), any()) } throws RuntimeException("API error")

        val result = provider.updateTask("p1", TaskUpdate(title = "Fail"))

        assertTrue(result.isFailure)
    }

    // --- deleteTask ---

    @Test
    fun `deleteTask archives the page`() = runTest {
        val bodySlot = slot<JsonObject>()
        coEvery { api.updatePage(eq("p1"), capture(bodySlot)) } returns buildPage("p1", archived = true)

        val result = provider.deleteTask("p1")

        assertTrue(result.isSuccess)
        assertTrue(bodySlot.captured.get("archived").asBoolean)
    }

    @Test
    fun `deleteTask returns failure on API error`() = runTest {
        coEvery { api.updatePage(any(), any()) } throws RuntimeException("API error")

        val result = provider.deleteTask("p1")

        assertTrue(result.isFailure)
    }

    // --- searchDatabases ---

    @Test
    fun `searchDatabases returns mapped databases`() = runTest {
        val bodySlot = slot<JsonObject>()
        coEvery { api.search(capture(bodySlot)) } returns NotionSearchResponse(
            results = listOf(
                NotionSearchResult(id = "db-1", title = listOf(NotionRichText("My Tasks")), `object` = "database"),
                NotionSearchResult(id = "db-2", title = listOf(NotionRichText("Work")), `object` = "database")
            )
        )

        val result = provider.searchDatabases()

        assertTrue(result.isSuccess)
        val databases = result.getOrNull()!!
        assertEquals(2, databases.size)
        assertEquals("My Tasks", databases[0].title)
        assertEquals("Work", databases[1].title)

        val body = bodySlot.captured
        val filter = body.getAsJsonObject("filter")
        assertEquals("database", filter.get("value").asString)
    }

    @Test
    fun `searchDatabases filters out non-database results`() = runTest {
        coEvery { api.search(any()) } returns NotionSearchResponse(
            results = listOf(
                NotionSearchResult(id = "db-1", title = listOf(NotionRichText("DB")), `object` = "database"),
                NotionSearchResult(id = "pg-1", title = listOf(NotionRichText("Page")), `object` = "page")
            )
        )

        val result = provider.searchDatabases()

        assertEquals(1, result.getOrNull()?.size)
    }

    @Test
    fun `searchDatabases returns failure on API error`() = runTest {
        coEvery { api.search(any()) } throws RuntimeException("API error")

        val result = provider.searchDatabases()

        assertTrue(result.isFailure)
    }
}
