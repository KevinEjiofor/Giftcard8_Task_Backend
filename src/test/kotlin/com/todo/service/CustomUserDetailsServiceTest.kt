package com.todo.service

import com.todo.data.models.User
import com.todo.data.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import kotlin.collections.contains

class CustomUserDetailsServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var customUserDetailsService: CustomUserDetailsService

    private val testUserEmail = "test@example.com"
    private val testUserId = "user123"
    private val testUser = User(
        id = testUserId,
        email = testUserEmail,
        username = "Test User",
        firstName = "tester",
        lastName = "king",
        password = "hashedPassword123"
    )

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        customUserDetailsService = CustomUserDetailsService(userRepository)
    }

    @Test
    fun `loadUserByUsername should return UserDetails when user exists`() {


        every { userRepository.findByEmail(testUserEmail) } returns testUser


        val result = customUserDetailsService.loadUserByUsername(testUserEmail)

        assertEquals(testUser.email, result.username)
        assertEquals(testUser.password, result.password)
        assertTrue(result.isEnabled)
        assertTrue(result.isAccountNonExpired)
        assertTrue(result.isAccountNonLocked)
        assertTrue(result.isCredentialsNonExpired)

        val authorities = result.authorities
        assertEquals(1, authorities.size)
        assertTrue(authorities.contains(SimpleGrantedAuthority("ROLE_USER")))

        verify { userRepository.findByEmail(testUserEmail) }
    }

    @Test
    fun `loadUserByUsername should throw UsernameNotFoundException when user doesn't exist`() {
        // Given
        every { userRepository.findByEmail(testUserEmail) } returns null

        // When & Then
        val exception = assertThrows<UsernameNotFoundException> {
            customUserDetailsService.loadUserByUsername(testUserEmail)
        }

        assertEquals("User not found with email: $testUserEmail", exception.message)
        verify { userRepository.findByEmail(testUserEmail) }
    }

    @Test
    fun `loadUserByUsername should handle different email formats`() {
        // Given
        val upperCaseEmail = "TEST@EXAMPLE.COM"
        val userWithUpperCaseEmail = testUser.copy(email = upperCaseEmail)
        every { userRepository.findByEmail(upperCaseEmail) } returns userWithUpperCaseEmail

        // When
        val result = customUserDetailsService.loadUserByUsername(upperCaseEmail)


        assertEquals(upperCaseEmail, result.username)
        assertEquals(testUser.password, result.password)

        verify { userRepository.findByEmail(upperCaseEmail) }
    }

    @Test
    fun `loadUserByUsername should handle empty email gracefully`() {

        val emptyEmail = ""
        every { userRepository.findByEmail(emptyEmail) } returns null

        val exception = assertThrows<UsernameNotFoundException> {
            customUserDetailsService.loadUserByUsername(emptyEmail)
        }

        assertEquals("User not found with email: ", exception.message)
        verify { userRepository.findByEmail(emptyEmail) }
    }

    @Test
    fun `loadUserByUsername should assign correct role to user`() {

        every { userRepository.findByEmail(testUserEmail) } returns testUser


        val result = customUserDetailsService.loadUserByUsername(testUserEmail)


        val authorities = result.authorities.map { it.authority }
        assertTrue(authorities.contains("ROLE_USER"))
        assertEquals(1, authorities.size)

        verify { userRepository.findByEmail(testUserEmail) }
    }

    @Test
    fun `loadUserByUsername should return UserDetails with correct account status`() {

        every { userRepository.findByEmail(testUserEmail) } returns testUser


        val result = customUserDetailsService.loadUserByUsername(testUserEmail)


        assertTrue(result.isEnabled)
        assertTrue(result.isAccountNonExpired)
        assertTrue(result.isAccountNonLocked)
        assertTrue(result.isCredentialsNonExpired)

        verify { userRepository.findByEmail(testUserEmail) }
    }
}