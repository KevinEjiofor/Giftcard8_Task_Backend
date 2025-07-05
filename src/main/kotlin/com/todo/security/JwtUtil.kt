package com.todo.security

import com.todo.data.models.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey
import java.util.Base64

@Component
class JwtUtil {

    private val logger: Logger = LoggerFactory.getLogger(JwtUtil::class.java)

    @Value("\${spring.security.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${spring.security.jwt.expiration}")
    private var jwtExpirationMs: Long = 0

    @Value("\${spring.security.jwt.refresh-expiration}")
    private var jwtRefreshExpirationMs: Long = 0

    private fun getSigningKey(): SecretKey {
        logger.debug("Getting signing key")
        val keyBytes = if (jwtSecret.length < 64) {
            logger.debug("JWT secret is too short, generating new key")
            Keys.secretKeyFor(SignatureAlgorithm.HS512).encoded
        } else {
            logger.debug("Using provided JWT secret")
            Base64.getDecoder().decode(jwtSecret)
        }
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateAccessToken(user: User): String {
        logger.debug("Generating access token for user: {}", user.email)
        return createToken(
            claims = mapOf("email" to user.email!!, "id" to user.id!!),
            subject = user.email,
            expirationMs = jwtExpirationMs
        )
    }

    fun generateRefreshToken(user: User): String {
        logger.debug("Generating refresh token for user: {}", user.email)
        return createToken(
            claims = mapOf("email" to user.email),
            subject = user.email,
            expirationMs = jwtRefreshExpirationMs
        )
    }

    fun generateTokenFromUserDetails(userDetails: UserDetails): String {
        logger.debug("Generating token from UserDetails for user: {}", userDetails.username)
        return createToken(
            claims = emptyMap(),
            subject = userDetails.username,
            expirationMs = jwtExpirationMs
        )
    }

    private fun createToken(claims: Map<String, Any>, subject: String?, expirationMs: Long): String {
        logger.debug("Creating token for subject: {}, expiration: {}ms", subject, expirationMs)
        val token = Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
        logger.debug("Token created successfully for subject: {}", subject)
        return token
    }

    fun getCurrentUserEmail(): String {
        logger.debug("Getting current user email from security context")
        val authentication = SecurityContextHolder.getContext().authentication
            ?: run {
                logger.warn("No authentication found in security context")
                throw RuntimeException("No authentication found")
            }

        // Check if user is authenticated and not anonymous
        if (!authentication.isAuthenticated || authentication.name == "anonymousUser") {
            logger.warn("User not authenticated or is anonymous user")
            throw RuntimeException("User not authenticated")
        }

        val principal = authentication.principal
        val email = when (principal) {
            is UserDetails -> {
                logger.debug("Principal is UserDetails, extracting username")
                principal.username
            }
            is String -> {
                logger.debug("Principal is String")
                principal
            }
            else -> {
                logger.error("Unable to extract user email from authentication, principal type: {}",
                    principal?.javaClass?.simpleName)
                throw RuntimeException("Unable to extract user email from authentication")
            }
        }

        // Additional check to ensure we don't return "anonymousUser"
        if (email.isNullOrBlank() || email == "anonymousUser") {
            logger.warn("Extracted email is null, blank, or anonymousUser")
            throw RuntimeException("User not authenticated")
        }

        logger.debug("Successfully extracted user email: {}", email)
        return email
    }

    fun getUsernameFromToken(token: String): String {
        logger.debug("Extracting username from token")
        return getClaimFromToken(token, Claims::getSubject)
    }

    fun getExpirationDateFromToken(token: String): Date {
        logger.debug("Extracting expiration date from token")
        return getClaimFromToken(token, Claims::getExpiration)
    }

    fun <T> getClaimFromToken(token: String, claimsResolver: (Claims) -> T): T {
        logger.debug("Extracting specific claim from token")
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        return try {
            logger.debug("Extracting all claims from token")
            val claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .body
            logger.debug("Successfully extracted claims. Subject: {}, Expiration: {}",
                claims.subject, claims.expiration)
            claims
        } catch (e: Exception) {
            logger.error("Error extracting claims from token: {}", e.message, e)
            throw e
        }
    }

    fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        val expired = expiration.before(Date())
        logger.debug("Token expiration check. Expires at: {}, Is expired: {}", expiration, expired)
        return expired
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        logger.debug("Validating token for user: {}", userDetails.username)
        val username = getUsernameFromToken(token)
        val isValid = username == userDetails.username && !isTokenExpired(token)
        logger.debug("Token validation result: {}", isValid)
        if (!isValid) {
            logger.warn("Token validation failed for user: {}", userDetails.username)
        }
        return isValid
    }

    fun extractUsername(token: String): String {
        logger.debug("Extracting username from token (alias for getUsernameFromToken)")
        return getUsernameFromToken(token)
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        logger.debug("Checking if token is valid (alias for validateToken)")
        return validateToken(token, userDetails)
    }

    fun getAccessTokenExpiration(): Long = jwtExpirationMs
    fun getRefreshTokenExpiration(): Long = jwtRefreshExpirationMs
}