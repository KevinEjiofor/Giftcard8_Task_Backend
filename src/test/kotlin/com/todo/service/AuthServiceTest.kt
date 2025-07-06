package com.todo.service

import com.todo.data.models.User
import com.todo.data.repository.UserRepository
import com.todo.dto.auth.*
import com.todo.exception.*
import com.todo.security.JwtUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Mock
    private lateinit var emailService: EmailService

    private lateinit var authService: AuthService

    private val testUser = User(
        id = "43jnrnrenjerkjwq",
        email = "test@example.com",
        username = "testuser",
        password = "encodedPassword",
        firstName = "Test",
        lastName = "User",
        emailVerified = true,
        emailVerificationToken = null,
        emailVerificationExpiry = null,
        passwordResetToken = null,
        passwordResetExpiry = null,
        refreshToken = null,
        refreshTokenExpiry = null,
        loginAttempts = 0,
        lockoutUntil = null,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        authService = AuthService(userRepository, passwordEncoder, jwtUtil, emailService)
        ReflectionTestUtils.setField(authService, "maxLoginAttempts", 5)
        ReflectionTestUtils.setField(authService, "lockoutDuration", 30L)
    }

    @Test
    fun `register should create user successfully`() {

        val request = RegisterRequest(
            email = "new@example.com",
            username = "newuser",
            password = "password123",
            firstName = "New",
            lastName = "User"
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(userRepository.existsByUsername(request.username)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.register(request)


        assertTrue(result.success)
        assertTrue(result.message.contains("Account created successfully"))

        verify(userRepository).save(any<User>())
        verify(emailService).sendVerificationEmail(eq(request.email), any(), eq(request.firstName))
    }

    @Test
    fun `register should throw UserAlreadyExistsException when email exists`() {

        val request = RegisterRequest(
            email = "existing@example.com",
            username = "newuser",
            password = "password123",
            firstName = "New",
            lastName = "User"
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(true)


        val exception = assertThrows<UserAlreadyExistsException> {
            authService.register(request)
        }
        assertEquals("User with this email already exists", exception.message)
    }

    @Test
    fun `register should throw UserAlreadyExistsException when username exists`() {

        val request = RegisterRequest(
            email = "new@example.com",
            username = "existinguser",
            password = "password123",
            firstName = "New",
            lastName = "User"
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(userRepository.existsByUsername(request.username)).thenReturn(true)


        val exception = assertThrows<UserAlreadyExistsException> {
            authService.register(request)
        }
        assertEquals("Username is already taken", exception.message)
    }

    @Test
    fun `register should throw EmailSendException when email sending fails`() {

        val request = RegisterRequest(
            email = "new@example.com",
            username = "newuser",
            password = "password123",
            firstName = "New",
            lastName = "User"
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(userRepository.existsByUsername(request.username)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }
        whenever(emailService.sendVerificationEmail(any(), any(), any())).thenThrow(RuntimeException())


        val exception = assertThrows<EmailSendException> {
            authService.register(request)
        }
        assertEquals("Failed to send verification email", exception.message)
    }

    @Test
    fun `login should return AuthResponse when credentials are valid`() {

        val request = LoginRequest(email = "test@example.com", password = "password123")
        val accessToken = "accessToken"
        val refreshToken = "refreshToken"

        whenever(userRepository.findByEmail(request.email)).thenReturn(testUser)
        whenever(passwordEncoder.matches(request.password, testUser.password)).thenReturn(true)
        whenever(jwtUtil.generateAccessToken(any())).thenReturn(accessToken)
        whenever(jwtUtil.generateRefreshToken(any())).thenReturn(refreshToken)
        whenever(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.login(request)


        assertEquals(accessToken, result.token)
        assertEquals(refreshToken, result.refreshToken)
        assertEquals(testUser.email, result.user.email)
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `login should throw InvalidCredentialsException when user not found`() {

        val request = LoginRequest(email = "nonexistent@example.com", password = "password123")

        whenever(userRepository.findByEmail(request.email)).thenReturn(null)


        val exception = assertThrows<InvalidCredentialsException> {
            authService.login(request)
        }
        assertEquals("Invalid email or password", exception.message)
    }

    @Test
    fun `login should throw AccountLockedException when account is locked`() {

        val request = LoginRequest(email = "test@example.com", password = "password123")
        val lockedUser = testUser.copy(
            lockoutUntil = LocalDateTime.now().plusMinutes(10)
        )

        whenever(userRepository.findByEmail(request.email)).thenReturn(lockedUser)


        val exception = assertThrows<AccountLockedException> {
            authService.login(request)
        }
        assertEquals("Account is locked due to multiple failed login attempts", exception.message)
    }

    @Test
    fun `login should throw InvalidCredentialsException when password is wrong`() {

        val request = LoginRequest(email = "test@example.com", password = "wrongpassword")

        whenever(userRepository.findByEmail(request.email)).thenReturn(testUser)
        whenever(passwordEncoder.matches(request.password, testUser.password)).thenReturn(false)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val exception = assertThrows<InvalidCredentialsException> {
            authService.login(request)
        }
        assertEquals("Invalid email or password", exception.message)
        verify(userRepository).save(any<User>()) // Verify failed login is handled
    }

    @Test
    fun `login should throw EmailNotVerifiedException when email not verified`() {

        val request = LoginRequest(email = "test@example.com", password = "password123")
        val unverifiedUser = testUser.copy(emailVerified = false)

        whenever(userRepository.findByEmail(request.email)).thenReturn(unverifiedUser)
        whenever(passwordEncoder.matches(request.password, unverifiedUser.password)).thenReturn(true)


        val exception = assertThrows<EmailNotVerifiedException> {
            authService.login(request)
        }
        assertEquals("Please verify your email address before logging in", exception.message)
    }

    @Test
    fun `forgotPassword should send reset email successfully`() {

        val request = ForgotPasswordRequest(email = "test@example.com")

        whenever(userRepository.findByEmail(request.email)).thenReturn(testUser)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.forgotPassword(request)


        assertTrue(result.success)
        assertTrue(result.message.contains("password reset code has been sent"))
        verify(userRepository).save(any<User>())
        verify(emailService).sendPasswordResetEmail(eq(request.email), any(), eq(testUser.firstName))
    }

    @Test
    fun `forgotPassword should throw UserNotFoundException when user not found`() {

        val request = ForgotPasswordRequest(email = "nonexistent@example.com")

        whenever(userRepository.findByEmail(request.email)).thenReturn(null)


        val exception = assertThrows<UserNotFoundException> {
            authService.forgotPassword(request)
        }
        assertEquals("User not found with this email", exception.message)
    }

    @Test
    fun `resetPassword should reset password successfully`() {

        val request = ResetPasswordRequest(token = "123456", newPassword = "newPassword123")
        val userWithResetToken = testUser.copy(
            passwordResetToken = "123456",
            passwordResetExpiry = LocalDateTime.now().plusHours(1)
        )

        whenever(userRepository.findByPasswordResetToken(request.token)).thenReturn(userWithResetToken)
        whenever(passwordEncoder.encode(request.newPassword)).thenReturn("encodedNewPassword")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.resetPassword(request)

        assertTrue(result.success)
        assertTrue(result.message.contains("password has been successfully reset"))
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `resetPassword should throw InvalidTokenException when token invalid`() {

        val request = ResetPasswordRequest(token = "invalidtoken", newPassword = "newPassword123")

        whenever(userRepository.findByPasswordResetToken(request.token)).thenReturn(null)


        val exception = assertThrows<InvalidTokenException> {
            authService.resetPassword(request)
        }
        assertEquals("Invalid or expired reset code", exception.message)
    }

    @Test
    fun `resetPassword should throw TokenExpiredException when token expired`() {

        val request = ResetPasswordRequest(token = "123456", newPassword = "newPassword123")
        val userWithExpiredToken = testUser.copy(
            passwordResetToken = "123456",
            passwordResetExpiry = LocalDateTime.now().minusHours(1)
        )

        whenever(userRepository.findByPasswordResetToken(request.token)).thenReturn(userWithExpiredToken)


        val exception = assertThrows<TokenExpiredException> {
            authService.resetPassword(request)
        }
        assertEquals("Password reset code has expired", exception.message)
    }

    @Test
    fun `verifyEmail should verify email successfully`() {

        val request = VerifyEmailRequest(token = "123456")
        val userWithVerificationToken = testUser.copy(
            emailVerified = false,
            emailVerificationToken = "123456",
            emailVerificationExpiry = LocalDateTime.now().plusHours(1)
        )

        whenever(userRepository.findByEmailVerificationToken(request.token)).thenReturn(userWithVerificationToken)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.verifyEmail(request)


        assertTrue(result.success)
        assertTrue(result.message.contains("email has been successfully verified"))
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `verifyEmail should throw InvalidTokenException when token invalid`() {

        val request = VerifyEmailRequest(token = "invalidtoken")

        whenever(userRepository.findByEmailVerificationToken(request.token)).thenReturn(null)


        val exception = assertThrows<InvalidTokenException> {
            authService.verifyEmail(request)
        }
        assertEquals("Invalid verification code", exception.message)
    }

    @Test
    fun `verifyEmail should throw TokenExpiredException when token expired`() {

        val request = VerifyEmailRequest(token = "123456")
        val userWithExpiredToken = testUser.copy(
            emailVerified = false,
            emailVerificationToken = "123456",
            emailVerificationExpiry = LocalDateTime.now().minusHours(1)
        )

        whenever(userRepository.findByEmailVerificationToken(request.token)).thenReturn(userWithExpiredToken)


        val exception = assertThrows<TokenExpiredException> {
            authService.verifyEmail(request)
        }
        assertEquals("Email verification code has expired", exception.message)
    }

    @Test
    fun `resendVerification should send new verification email`() {

        val request = ResendVerificationRequest(email = "test@example.com")
        val unverifiedUser = testUser.copy(emailVerified = false)

        whenever(userRepository.findByEmail(request.email)).thenReturn(unverifiedUser)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.resendVerification(request)


        assertTrue(result.success)
        assertTrue(result.message.contains("verification code has been sent"))
        verify(userRepository).save(any<User>())
        verify(emailService).sendVerificationEmail(eq(request.email), any(), eq(unverifiedUser.firstName))
    }

    @Test
    fun `resendVerification should throw RuntimeException when email already verified`() {

        val request = ResendVerificationRequest(email = "test@example.com")

        whenever(userRepository.findByEmail(request.email)).thenReturn(testUser)


        val exception = assertThrows<RuntimeException> {
            authService.resendVerification(request)
        }
        assertEquals("Email is already verified", exception.message)
    }

    @Test
    fun `refreshToken should return new tokens successfully`() {

        val request = RefreshTokenRequest(refreshToken = "validRefreshToken")
        val userWithRefreshToken = testUser.copy(
            refreshToken = "validRefreshToken",
            refreshTokenExpiry = LocalDateTime.now().plusDays(1)
        )
        val newAccessToken = "newAccessToken"
        val newRefreshToken = "newRefreshToken"

        whenever(userRepository.findByRefreshToken(request.refreshToken)).thenReturn(userWithRefreshToken)
        whenever(jwtUtil.generateAccessToken(any())).thenReturn(newAccessToken)
        whenever(jwtUtil.generateRefreshToken(any())).thenReturn(newRefreshToken)
        whenever(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.refreshToken(request)


        assertEquals(newAccessToken, result.token)
        assertEquals(newRefreshToken, result.refreshToken)
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `refreshToken should throw InvalidTokenException when token invalid`() {

        val request = RefreshTokenRequest(refreshToken = "invalidToken")

        whenever(userRepository.findByRefreshToken(request.refreshToken)).thenReturn(null)


        val exception = assertThrows<InvalidTokenException> {
            authService.refreshToken(request)
        }
        assertEquals("Invalid refresh token", exception.message)
    }

    @Test
    fun `refreshToken should throw TokenExpiredException when token expired`() {

        val request = RefreshTokenRequest(refreshToken = "expiredToken")
        val userWithExpiredToken = testUser.copy(
            refreshToken = "expiredToken",
            refreshTokenExpiry = LocalDateTime.now().minusDays(1)
        )

        whenever(userRepository.findByRefreshToken(request.refreshToken)).thenReturn(userWithExpiredToken)


        val exception = assertThrows<TokenExpiredException> {
            authService.refreshToken(request)
        }
        assertEquals("Refresh token has expired", exception.message)
    }

    @Test
    fun `logout should clear refresh token successfully`() {

        val request = LogoutRequest(refreshToken = "validRefreshToken")
        val userWithRefreshToken = testUser.copy(refreshToken = "validRefreshToken")

        whenever(userRepository.findByRefreshToken(request.refreshToken)).thenReturn(userWithRefreshToken)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.logout(request)


        assertTrue(result.success)
        assertTrue(result.message.contains("successfully logged out"))
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `logout should succeed even when refresh token not found`() {

        val request = LogoutRequest(refreshToken = "nonexistentToken")

        whenever(userRepository.findByRefreshToken(request.refreshToken)).thenReturn(null)


        val result = authService.logout(request)


        assertTrue(result.success)
        assertTrue(result.message.contains("successfully logged out"))
        verify(userRepository, never()).save(any<User>())
    }

    @Test
    fun `login should handle lockout correctly after max attempts`() {

        val request = LoginRequest(email = "test@example.com", password = "wrongpassword")
        val userWithAttempts = testUser.copy(loginAttempts = 4) // One less than max

        whenever(userRepository.findByEmail(request.email)).thenReturn(userWithAttempts)
        whenever(passwordEncoder.matches(request.password, userWithAttempts.password)).thenReturn(false)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        assertThrows<InvalidCredentialsException> {
            authService.login(request)
        }


        verify(userRepository).save(argThat<User> { user ->
            user.loginAttempts == 5 && user.lockoutUntil != null
        })
    }

    @Test
    fun `login should reset attempts on successful login`() {

        val request = LoginRequest(email = "test@example.com", password = "password123")
        val userWithAttempts = testUser.copy(loginAttempts = 3)
        val accessToken = "accessToken"
        val refreshToken = "refreshToken"

        whenever(userRepository.findByEmail(request.email)).thenReturn(userWithAttempts)
        whenever(passwordEncoder.matches(request.password, userWithAttempts.password)).thenReturn(true)
        whenever(jwtUtil.generateAccessToken(any())).thenReturn(accessToken)
        whenever(jwtUtil.generateRefreshToken(any())).thenReturn(refreshToken)
        whenever(jwtUtil.getAccessTokenExpiration()).thenReturn(3600000L)
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] }


        val result = authService.login(request)


        assertEquals(accessToken, result.token)
        verify(userRepository).save(argThat<User> { user ->
            user.loginAttempts == 0 && user.lockoutUntil == null
        })
    }
}