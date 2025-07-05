package com.todo.dto.task


data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val completed: Boolean? = null
)
data class TaskUpdatedResponse(
    val message: String,
    val success: Boolean,
    val task: TaskResponse
)
