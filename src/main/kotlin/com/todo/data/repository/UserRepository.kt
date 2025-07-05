package com.todo.data.repository

import com.todo.data.models.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String?): User?
    fun findByUsername(username: String): User?
    fun findByEmailVerificationToken(token: String): User?
    fun findByPasswordResetToken(token: String): User?
    fun findByRefreshToken(token: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByEmailAndLockoutUntilBefore(email: String, lockoutUntil: LocalDateTime): User?
}