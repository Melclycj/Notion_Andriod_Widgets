package com.notionwidgets.domain.usecase

import com.notionwidgets.domain.model.Task
import com.notionwidgets.domain.repository.TaskRepository
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Task> {
        return repository.completeTask(taskId)
    }
}
