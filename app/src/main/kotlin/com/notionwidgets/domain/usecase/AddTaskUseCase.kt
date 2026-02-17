package com.notionwidgets.domain.usecase

import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(databaseId: String, title: String): Result<Task> {
        return repository.addTask(databaseId, title)
    }
}
