package com.todo.dto.auth


data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val user: UserResponse,
    val expiresAt: Long
)


