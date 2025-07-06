package com.todo.data.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    val email: String,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,


    val emailVerified: Boolean = false,
    val emailVerificationToken: String? = null,
    val emailVerificationExpiry: LocalDateTime? = null,


    val passwordResetToken: String? = null,
    val passwordResetExpiry: LocalDateTime? = null,


    val refreshToken: String? = null,
    val refreshTokenExpiry: LocalDateTime? = null,


    val loginAttempts: Int = 0,
    val lockoutUntil: LocalDateTime? = null,


    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)