package com.todo.dto.task

import jakarta.validation.constraints.NotBlank

data class CreateTaskRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null
)


data class TaskCreatedResponse(
    val message: String,
    val success: Boolean,
    val task: TaskResponse
)