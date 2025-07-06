package com.todo.data.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

@Document(collection = "tasks")
data class Task(
    @Id
    val id: String? = null,

    @field:NotBlank(message = "Title is required")
    val title: String,

    val description: String? = null,

    val completed: Boolean = false,

    @field:NotBlank(message = "User ID is required")
    val userId: String,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)