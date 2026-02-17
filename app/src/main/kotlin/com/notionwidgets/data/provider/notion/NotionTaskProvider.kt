package com.notionwidgets.data.provider.notion

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.notionwidgets.data.provider.NotionDatabase
import com.notionwidgets.data.provider.TaskProvider
import com.notionwidgets.domain.model.ProviderType
import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.model.TaskUpdate
import javax.inject.Inject

class NotionTaskProvider @Inject constructor(
    private val api: NotionApiService,
    private val mapper: NotionMapper
) : TaskProvider {

    override val providerType: ProviderType = ProviderType.NOTION

    override suspend fun fetchTasks(databaseId: String): Result<List<Task>> {
        return runCatching {
            val response = api.queryDatabase(databaseId)
            response.results
                .filter { !it.archived }
                .map { mapper.pageToTask(it, databaseId) }
        }
    }

    override suspend fun addTask(databaseId: String, title: String): Result<Task> {
        return runCatching {
            val body = buildCreatePageBody(databaseId, title)
            val page = api.createPage(body)
            mapper.pageToTask(page, databaseId)
        }
    }

    override suspend fun updateTask(taskId: String, updates: TaskUpdate): Result<Task> {
        return runCatching {
            val body = buildUpdatePageBody(updates)
            val page = api.updatePage(taskId, body)
            mapper.pageToTask(page, "")
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return runCatching {
            val body = JsonObject().apply {
                addProperty("archived", true)
            }
            api.updatePage(taskId, body)
        }
    }

    override suspend fun searchDatabases(): Result<List<NotionDatabase>> {
        return runCatching {
            val body = JsonObject().apply {
                addProperty("filter", "database")
            }
            // Notion search filter is a nested object, not a string
            val searchBody = JsonObject().apply {
                add("filter", JsonObject().apply {
                    addProperty("value", "database")
                    addProperty("property", "object")
                })
            }
            val response = api.search(searchBody)
            response.results.mapNotNull { mapper.searchResultToDatabase(it) }
        }
    }

    private fun buildCreatePageBody(databaseId: String, title: String): JsonObject {
        return JsonObject().apply {
            add("parent", JsonObject().apply {
                addProperty("database_id", databaseId)
            })
            add("properties", JsonObject().apply {
                add("Name", JsonObject().apply {
                    add("title", JsonArray().apply {
                        add(JsonObject().apply {
                            add("text", JsonObject().apply {
                                addProperty("content", title)
                            })
                        })
                    })
                })
            })
        }
    }

    private fun buildUpdatePageBody(updates: TaskUpdate): JsonObject {
        return JsonObject().apply {
            val props = JsonObject()
            updates.title?.let { newTitle ->
                props.add("Name", JsonObject().apply {
                    add("title", JsonArray().apply {
                        add(JsonObject().apply {
                            add("text", JsonObject().apply {
                                addProperty("content", newTitle)
                            })
                        })
                    })
                })
            }
            updates.isCompleted?.let { completed ->
                props.add("Done", JsonObject().apply {
                    addProperty("checkbox", completed)
                })
            }
            add("properties", props)
        }
    }
}
