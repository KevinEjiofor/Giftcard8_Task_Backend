package com.todo.dto.auth

import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank(message = "Token is required")
    val refreshToken: String
)