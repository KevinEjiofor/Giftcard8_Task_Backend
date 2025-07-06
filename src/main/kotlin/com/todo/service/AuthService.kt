package com.todo.service

import com.todo.data.models.User
import com.todo.data.repository.UserRepository
import com.todo.dto.auth.*
import com.todo.dto.profile.ProfileResponse
import com.todo.exception.*
import com.todo.security.JwtUtil
import org.springframework.beans.factory.annotation.Value

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

import kotlin.random.Random

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val emailService: EmailService
) {

    @Value("\${app.security.max-login-attempts}")
    private val maxLoginAttempts: Int = 5

    @Value("\${app.security.lockout-duration}")
    private val lockoutDuration: Long = 30

    private fun generateSixDigitToken(): String {
        return Random.nextInt(100000, 999999).toString()
    }


    fun register(request: RegisterRequest): MessageResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw UserAlreadyExistsException("User with this email already exists")
        }

        if (userRepository.existsByUsername(request.username)) {
            throw UserAlreadyExistsException("Username is already taken")
        }

        val verificationToken = generateSixDigitToken()

        val user = User(
            email = request.email,
            username = request.username,
            password = passwordEncoder.encode(request.password),
            firstName = request.firstName,
            lastName = request.lastName,
            emailVerificationToken = verificationToken,
            emailVerificationExpiry = LocalDateTime.now().plusHours(24)
        )

        val savedUser = userRepository.save(user)

        try {
            emailService.sendVerificationEmail(request.email, verificationToken, request.firstName)
        } catch (e: Exception) {
            throw EmailSendException("Failed to send verification email")
        }


        return MessageResponse(
            message = "🎉 Account created successfully! Please check your email for a 6-digit verification code to activate your account. The code will expire in 24 hours.",
            success = true
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException("Invalid email or password")

        if (user.lockoutUntil != null && user.lockoutUntil.isAfter(LocalDateTime.now())) {
            throw AccountLockedException("Account is locked due to multiple failed login attempts")
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            handleFailedLogin(user)
            throw InvalidCredentialsException("Invalid email or password")
        }

        if (!user.emailVerified) {
            throw EmailNotVerifiedException("Please verify your email address before logging in")
        }

        val accessToken = jwtUtil.generateAccessToken(user)
        val refreshToken = jwtUtil.generateRefreshToken(user)

        val updatedUser = user.copy(
            loginAttempts = 0,
            lockoutUntil = null,
            refreshToken = refreshToken,
            refreshTokenExpiry = LocalDateTime.now().plusDays(7),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(updatedUser)

        return AuthResponse(
            token = accessToken,
            refreshToken = refreshToken,
            user = mapToUserResponse(updatedUser),
            expiresAt = System.currentTimeMillis() + jwtUtil.getAccessTokenExpiration()
        )
    }

    fun forgotPassword(request: ForgotPasswordRequest): MessageResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UserNotFoundException("User not found with this email")

        val resetToken = generateSixDigitToken()

        val updatedUser = user.copy(
            passwordResetToken = resetToken,
            passwordResetExpiry = LocalDateTime.now().plusHours(1),
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        try {
            emailService.sendPasswordResetEmail(request.email, resetToken, user.firstName)
        } catch (e: Exception) {
            throw EmailSendException("Failed to send password reset email")
        }

        return MessageResponse(
            message = "🔐 A 6-digit password reset code has been sent to your email! Enter the code to reset your password.",
            success = true
        )
    }

    fun resetPassword(request: ResetPasswordRequest): MessageResponse {
        val user = userRepository.findByPasswordResetToken(request.token)
            ?: throw InvalidTokenException("Invalid or expired reset code")

        if (user.passwordResetExpiry == null || user.passwordResetExpiry.isBefore(LocalDateTime.now())) {
            throw TokenExpiredException("Password reset code has expired")
        }

        val updatedUser = user.copy(
            password = passwordEncoder.encode(request.newPassword),
            passwordResetToken = null,
            passwordResetExpiry = null,
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        return MessageResponse(
            message = "🎉 Your password has been successfully reset! You can now log in with your new password. Welcome back!",
            success = true
        )
    }


    fun verifyEmail(request: VerifyEmailRequest): MessageResponse {
        val user = userRepository.findByEmailVerificationToken(request.token)
            ?: throw InvalidTokenException("Invalid verification code")

        if (user.emailVerificationExpiry == null || user.emailVerificationExpiry.isBefore(LocalDateTime.now())) {
            throw TokenExpiredException("Email verification code has expired")
        }

        val updatedUser = user.copy(
            emailVerified = true,
            emailVerificationToken = null,
            emailVerificationExpiry = null,
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        return MessageResponse(
            message = "🎉 Your email has been successfully verified! Welcome to our platform! You can now log in to access all features.",
            success = true
        )
    }

    fun resendVerification(request: ResendVerificationRequest): MessageResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UserNotFoundException("User not found with this email")

        if (user.emailVerified) {
            throw RuntimeException("Email is already verified")
        }

        val verificationToken = generateSixDigitToken()

        val updatedUser = user.copy(
            emailVerificationToken = verificationToken,
            emailVerificationExpiry = LocalDateTime.now().plusHours(24),
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)

        try {
            emailService.sendVerificationEmail(request.email, verificationToken, user.firstName)
        } catch (e: Exception) {
            throw EmailSendException("Failed to send verification email")
        }

        return MessageResponse(
            message = "📧 A fresh 6-digit verification code has been sent to your inbox! Please enter the code to activate your account.",
            success = true
        )
    }

    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val user = userRepository.findByRefreshToken(request.refreshToken)
            ?: throw InvalidTokenException("Invalid refresh token")

        if (user.refreshTokenExpiry == null || user.refreshTokenExpiry.isBefore(LocalDateTime.now())) {
            throw TokenExpiredException("Refresh token has expired")
        }

        val accessToken = jwtUtil.generateAccessToken(user)
        val newRefreshToken = jwtUtil.generateRefreshToken(user)

        val updatedUser = user.copy(
            refreshToken = newRefreshToken,
            refreshTokenExpiry = LocalDateTime.now().plusDays(7),
            updatedAt = LocalDateTime.now()
        )
        userRepository.save(updatedUser)

        return AuthResponse(
            token = accessToken,
            refreshToken = newRefreshToken,
            user = mapToUserResponse(updatedUser),
            expiresAt = System.currentTimeMillis() + jwtUtil.getAccessTokenExpiration()
        )
    }
    fun getUserProfile(email: String): ProfileResponse {
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
    fun logout(request: LogoutRequest): MessageResponse {
        val user = userRepository.findByRefreshToken(request.refreshToken)
        if (user != null) {
            val updatedUser = user.copy(
                refreshToken = null,
                refreshTokenExpiry = null,
                updatedAt = LocalDateTime.now()
            )
            userRepository.save(updatedUser)
        }

        return MessageResponse(
            message = "👋 You have been successfully logged out! Thanks for using our service. See you again soon!",
            success = true
        )
    }


    private fun handleFailedLogin(user: User) {
        val attempts = user.loginAttempts + 1
        val lockoutUntil = if (attempts >= maxLoginAttempts) {
            LocalDateTime.now().plusMinutes(lockoutDuration)
        } else null

        val updatedUser = user.copy(
            loginAttempts = attempts,
            lockoutUntil = lockoutUntil,
            updatedAt = LocalDateTime.now()
        )

        userRepository.save(updatedUser)
    }

    private fun mapToUserResponse(user: User): UserResponse {
        return UserResponse(
            email = user.email,
            username = user.username,
            firstName = user.firstName,
            lastName = user.lastName,
            emailVerified = user.emailVerified,
            createdAt = user.createdAt.toString()
        )
    }
}