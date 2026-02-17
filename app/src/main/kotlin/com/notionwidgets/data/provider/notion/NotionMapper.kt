package com.notionwidgets.data.provider.notion

import com.notionwidgets.data.provider.NotionDatabase
import com.notionwidgets.domain.model.ProviderType
import com.notionwidgets.domain.model.Task
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class NotionMapper @Inject constructor() {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun pageToTask(page: NotionPage, databaseId: String): Task {
        val title = extractTitle(page.properties)
        val isCompleted = extractCheckbox(page.properties)
        val createdAt = parseTimestamp(page.createdTime)
        val updatedAt = parseTimestamp(page.lastEditedTime)

        return Task(
            id = page.id,
            title = title,
            isCompleted = isCompleted,
            createdAt = createdAt,
            updatedAt = updatedAt,
            databaseId = databaseId,
            providerType = ProviderType.NOTION
        )
    }

    fun searchResultToDatabase(result: NotionSearchResult): NotionDatabase? {
        if (result.`object` != "database") return null
        val title = result.title.firstOrNull()?.plainText ?: "Untitled"
        return NotionDatabase(id = result.id, title = title)
    }

    private fun extractTitle(properties: Map<String, com.google.gson.JsonObject>): String {
        for ((_, value) in properties) {
            if (value.has("type") && value.get("type").asString == "title") {
                val titleArray = value.getAsJsonArray("title") ?: continue
                return titleArray.joinToString("") { element ->
                    element.asJsonObject.get("plain_text")?.asString ?: ""
                }
            }
        }
        return "Untitled"
    }

    private fun extractCheckbox(properties: Map<String, com.google.gson.JsonObject>): Boolean {
        for ((_, value) in properties) {
            if (value.has("type") && value.get("type").asString == "checkbox") {
                return value.get("checkbox")?.asBoolean ?: false
            }
        }
        return false
    }

    private fun parseTimestamp(isoString: String): Long {
        return try {
            isoFormat.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
}
