package com.todo.dto.task

import java.time.LocalDateTime

data class TaskResponse(
    val id: String,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
data class TasksResponse(
    val message: String,
    val success: Boolean,
    val tasks: List<TaskResponse>,
    val totalTasks: Int
)