package com.notionwidgets.domain.usecase

import com.notionwidgets.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        return repository.deleteTask(taskId)
    }
}
