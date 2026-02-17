package com.notionwidgets.domain.model

data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val databaseId: String = "",
    val providerType: ProviderType = ProviderType.NOTION
)
