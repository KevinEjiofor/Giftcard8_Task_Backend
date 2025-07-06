package com.todo.dto.profile

data class ProfileResponse(
    val id: String?,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val emailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)