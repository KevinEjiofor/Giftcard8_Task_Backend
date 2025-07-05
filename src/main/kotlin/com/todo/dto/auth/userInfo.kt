package com.todo.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class UserInfo(

    @JsonProperty("email")
    val email: String,

    @JsonProperty("username")
    val username: String? = null,

    @JsonProperty("first_name")
    val firstName: String? = null,

    @JsonProperty("last_name")
    val lastName: String? = null,

    @JsonProperty("email_verified")
    val emailVerified: Boolean = false,

    @JsonProperty("created_at")
    val createdAt: LocalDateTime
)