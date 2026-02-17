package com.notionwidgets.data.provider.notion

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.notionwidgets.domain.model.ProviderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotionMapperTest {

    private lateinit var mapper: NotionMapper

    @Before
    fun setUp() {
        mapper = NotionMapper()
    }

    @Test
    fun `pageToTask maps title and checkbox correctly`() {
        val page = NotionPage(
            id = "page-123",
            properties = mapOf(
                "Name" to buildTitleProperty("Buy groceries"),
                "Done" to buildCheckboxProperty(false)
            ),
            createdTime = "2025-01-15T10:30:00.000Z",
            lastEditedTime = "2025-01-15T11:00:00.000Z"
        )

        val task = mapper.pageToTask(page, "db-456")

        assertEquals("page-123", task.id)
        assertEquals("Buy groceries", task.title)
        assertEquals(false, task.isCompleted)
        assertEquals("db-456", task.databaseId)
        assertEquals(ProviderType.NOTION, task.providerType)
    }

    @Test
    fun `pageToTask maps completed checkbox`() {
        val page = NotionPage(
            id = "page-789",
            properties = mapOf(
                "Name" to buildTitleProperty("Done task"),
                "Status" to buildCheckboxProperty(true)
            ),
            createdTime = "2025-01-15T10:30:00.000Z",
            lastEditedTime = "2025-01-15T11:00:00.000Z"
        )

        val task = mapper.pageToTask(page, "db-456")

        assertTrue(task.isCompleted)
    }

    @Test
    fun `pageToTask defaults to Untitled when no title property`() {
        val page = NotionPage(
            id = "page-no-title",
            properties = mapOf(
                "Done" to buildCheckboxProperty(false)
            ),
            createdTime = "2025-01-15T10:30:00.000Z",
            lastEditedTime = "2025-01-15T11:00:00.000Z"
        )

        val task = mapper.pageToTask(page, "db-456")

        assertEquals("Untitled", task.title)
    }

    @Test
    fun `pageToTask defaults to false when no checkbox property`() {
        val page = NotionPage(
            id = "page-no-check",
            properties = mapOf(
                "Name" to buildTitleProperty("Task without checkbox")
            ),
            createdTime = "2025-01-15T10:30:00.000Z",
            lastEditedTime = "2025-01-15T11:00:00.000Z"
        )

        val task = mapper.pageToTask(page, "db-456")

        assertEquals(false, task.isCompleted)
    }

    @Test
    fun `pageToTask parses timestamps correctly`() {
        val page = NotionPage(
            id = "page-time",
            properties = emptyMap(),
            createdTime = "2025-06-15T12:00:00.000Z",
            lastEditedTime = "2025-06-15T14:30:00.000Z"
        )

        val task = mapper.pageToTask(page, "db-1")

        // Timestamps should be different (not default)
        assertTrue(task.createdAt < task.updatedAt)
    }

    @Test
    fun `pageToTask handles malformed timestamp gracefully`() {
        val page = NotionPage(
            id = "page-bad-time",
            properties = emptyMap(),
            createdTime = "not-a-date",
            lastEditedTime = "also-not-a-date"
        )

        val task = mapper.pageToTask(page, "db-1")

        // Should fall back to current time without crashing
        assertTrue(task.createdAt > 0)
    }

    @Test
    fun `pageToTask concatenates multi-segment title`() {
        val titleProp = JsonObject().apply {
            addProperty("type", "title")
            add("title", JsonArray().apply {
                add(JsonObject().apply { addProperty("plain_text", "Hello ") })
                add(JsonObject().apply { addProperty("plain_text", "World") })
            })
        }

        val page = NotionPage(
            id = "page-multi",
            properties = mapOf("Name" to titleProp),
            createdTime = "2025-01-15T10:30:00.000Z",
            lastEditedTime = "2025-01-15T11:00:00.000Z"
        )

        val task = mapper.pageToTask(page, "db-1")

        assertEquals("Hello World", task.title)
    }

    @Test
    fun `searchResultToDatabase maps database result`() {
        val result = NotionSearchResult(
            id = "db-abc",
            title = listOf(NotionRichText(plainText = "My Tasks")),
            `object` = "database"
        )

        val db = mapper.searchResultToDatabase(result)

        assertEquals("db-abc", db?.id)
        assertEquals("My Tasks", db?.title)
    }

    @Test
    fun `searchResultToDatabase returns null for non-database`() {
        val result = NotionSearchResult(
            id = "page-xyz",
            title = listOf(NotionRichText(plainText = "A Page")),
            `object` = "page"
        )

        assertNull(mapper.searchResultToDatabase(result))
    }

    @Test
    fun `searchResultToDatabase defaults to Untitled when no title`() {
        val result = NotionSearchResult(
            id = "db-empty",
            title = emptyList(),
            `object` = "database"
        )

        val db = mapper.searchResultToDatabase(result)

        assertEquals("Untitled", db?.title)
    }

    private fun buildTitleProperty(text: String): JsonObject {
        return JsonObject().apply {
            addProperty("type", "title")
            add("title", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("plain_text", text)
                })
            })
        }
    }

    private fun buildCheckboxProperty(checked: Boolean): JsonObject {
        return JsonObject().apply {
            addProperty("type", "checkbox")
            addProperty("checkbox", checked)
        }
    }
}
