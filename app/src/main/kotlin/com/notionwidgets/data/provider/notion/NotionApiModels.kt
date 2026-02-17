package com.notionwidgets.data.provider.notion

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class NotionQueryResponse(
    val results: List<NotionPage>,
    @SerializedName("has_more") val hasMore: Boolean = false,
    @SerializedName("next_cursor") val nextCursor: String? = null
)

data class NotionPage(
    val id: String,
    val properties: Map<String, JsonObject> = emptyMap(),
    @SerializedName("created_time") val createdTime: String = "",
    @SerializedName("last_edited_time") val lastEditedTime: String = "",
    val archived: Boolean = false
)

data class NotionSearchResponse(
    val results: List<NotionSearchResult>,
    @SerializedName("has_more") val hasMore: Boolean = false
)

data class NotionSearchResult(
    val id: String,
    val title: List<NotionRichText> = emptyList(),
    val `object`: String = ""
)

data class NotionRichText(
    @SerializedName("plain_text") val plainText: String = ""
)

data class NotionOAuthTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("workspace_id") val workspaceId: String = "",
    @SerializedName("bot_id") val botId: String = ""
)
