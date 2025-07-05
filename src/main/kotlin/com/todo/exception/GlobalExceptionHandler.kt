package com.todo.exception

import com.todo.dto.common.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.fieldErrors.forEach { error ->
            errors[error.field] = error.defaultMessage ?: "Invalid value"
        }

        val errorResponse = ErrorResponse(
            message = "Validation failed",
            details = errors,
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value()
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    // Auth-related exceptions
    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "User already exists",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = "Invalid email or password",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(EmailNotVerifiedException::class)
    fun handleEmailNotVerifiedException(ex: EmailNotVerifiedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Email not verified",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(AccountLockedException::class)
    fun handleAccountLockedException(ex: AccountLockedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Account is locked",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.LOCKED.value()
        )
        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse)
    }

    @ExceptionHandler(TokenExpiredException::class)
    fun handleTokenExpiredException(ex: TokenExpiredException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Token has expired",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(ex: InvalidTokenException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Invalid token",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "User not found",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(EmailSendException::class)
    fun handleEmailSendException(ex: EmailSendException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = "Failed to send email. Please try again later.",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(PasswordMismatchException::class)
    fun handlePasswordMismatchException(ex: PasswordMismatchException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Current password is incorrect",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(AuthenticationRequiredException::class)
    fun handleAuthenticationRequiredException(ex: AuthenticationRequiredException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Authentication required",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }


    @ExceptionHandler(TaskNotFoundException::class)
    fun handleTaskNotFoundException(ex: TaskNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Task not found",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(TaskAccessDeniedException::class)
    fun handleTaskAccessDeniedException(ex: TaskAccessDeniedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "You don't have permission to access this task",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(InvalidTaskDataException::class)
    fun handleInvalidTaskDataException(ex: InvalidTaskDataException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Invalid task data provided",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }


    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = "Invalid email or password",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }


    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = "An unexpected error occurred",
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}