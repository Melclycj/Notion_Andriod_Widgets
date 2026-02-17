package com.notionwidgets.widget

import com.notionwidgets.data.auth.AuthManager
import com.notionwidgets.domain.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun taskRepository(): TaskRepository
    fun authManager(): AuthManager
}
