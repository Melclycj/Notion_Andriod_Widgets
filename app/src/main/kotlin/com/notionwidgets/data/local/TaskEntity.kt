package com.notionwidgets.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.notionwidgets.domain.model.ProviderType
import com.notionwidgets.domain.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val databaseId: String,
    val providerType: String
) {
    fun toDomain(): Task = Task(
        id = id,
        title = title,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        databaseId = databaseId,
        providerType = ProviderType.valueOf(providerType)
    )

    companion object {
        fun fromDomain(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            isCompleted = task.isCompleted,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            databaseId = task.databaseId,
            providerType = task.providerType.name
        )
    }
}
