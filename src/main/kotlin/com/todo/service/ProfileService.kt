package com.todo.service

import com.todo.dto.auth.MessageResponse



import com.todo.data.repository.UserRepository
import com.todo.dto.profile.ProfileResponse
import com.todo.dto.profile.UpdateProfileRequest
import com.todo.exception.UserNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun getProfile(email: String): ProfileResponse {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException("User not found")

        return ProfileResponse(
            id = user.id,
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            emailVerified = user.emailVerified,
            createdAt = user.createdAt.toString(),
            updatedAt = user.updatedAt.toString()
        )
    }

    fun updateProfile(email: String, request: UpdateProfileRequest): MessageResponse {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException("User not found")

        // Check if username is being changed and if it's already taken
        if (request.username != user.username && userRepository.existsByUsername(request.username)) {
            throw RuntimeException("Username is already taken")
        }

        val updatedUser = user.copy(
            username = request.username,
            firstName = request.firstName,
            lastName = request.lastName,
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        return MessageResponse(
            message = "✅ Profile updated successfully!",
            success = true
        )
    }

    fun deleteAccount(email: String): MessageResponse {
        val user = userRepository.findByEmail(email)
            ?: throw UserNotFoundException("User not found")

        userRepository.delete(user)

        return MessageResponse(
            message = "🗑️ Account deleted successfully. We're sorry to see you go!",
            success = true
        )
    }
}
