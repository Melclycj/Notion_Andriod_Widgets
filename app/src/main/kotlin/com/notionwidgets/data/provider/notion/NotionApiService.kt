package com.notionwidgets.data.provider.notion

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotionApiService {

    @POST("v1/databases/{databaseId}/query")
    suspend fun queryDatabase(
        @Path("databaseId") databaseId: String,
        @Body body: JsonObject = JsonObject()
    ): NotionQueryResponse

    @POST("v1/pages")
    suspend fun createPage(
        @Body body: JsonObject
    ): NotionPage

    @PATCH("v1/pages/{pageId}")
    suspend fun updatePage(
        @Path("pageId") pageId: String,
        @Body body: JsonObject
    ): NotionPage

    @POST("v1/search")
    suspend fun search(
        @Body body: JsonObject
    ): NotionSearchResponse

    @POST("v1/oauth/token")
    suspend fun exchangeToken(
        @Body body: JsonObject
    ): NotionOAuthTokenResponse

    @GET("v1/users/me")
    suspend fun getMe(): JsonObject
}
