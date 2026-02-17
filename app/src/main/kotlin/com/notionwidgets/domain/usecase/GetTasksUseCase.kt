package com.notionwidgets.domain.usecase

import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.repository.TaskRepository
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(databaseId: String): Result<List<Task>> {
        return repository.getTasks(databaseId)
    }
}
