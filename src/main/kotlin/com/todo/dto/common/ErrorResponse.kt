package com.todo.dto.common

import java.time.LocalDateTime

data class ErrorResponse(
    val message: String,
    val details: Map<String, String>? = null,
    val timestamp: LocalDateTime,
    val status: Int
)