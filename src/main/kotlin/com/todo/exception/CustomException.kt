package com.todo.exception

class UserAlreadyExistsException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class EmailNotVerifiedException(message: String) : RuntimeException(message)
class AccountLockedException(message: String) : RuntimeException(message)
class TokenExpiredException(message: String) : RuntimeException(message)
class InvalidTokenException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class EmailSendException(message: String) : RuntimeException(message)
class PasswordMismatchException(message: String) : RuntimeException(message)
class AuthenticationRequiredException(message: String) : RuntimeException(message)

class TaskNotFoundException(message: String) : RuntimeException(message)

class TaskAccessDeniedException(message: String) : RuntimeException(message)

class InvalidTaskDataException(message: String) : RuntimeException(message)