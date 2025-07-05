package com.todo.dto.auth


data class UserResponse(

    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val emailVerified: Boolean,
    val createdAt: String
)
