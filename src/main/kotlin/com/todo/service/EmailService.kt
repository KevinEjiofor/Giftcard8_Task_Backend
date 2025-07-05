package com.todo.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val javaMailSender: JavaMailSender
) {

    @Value("\${spring.mail.username}")
    private lateinit var fromEmail: String

    @Value("\${app.frontend.url}")
    private lateinit var frontendUrl: String

    fun sendVerificationEmail(email: String, token: String, firstName: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            setFrom(fromEmail)
            subject = "Verify Your Email Address"
            text = """
                Hi $firstName,
                
                Thank you for registering with our Todo App!
                
                Please use the following 6-digit verification code to verify your email address:
                
                VERIFICATION CODE: $token
                
                Enter this code in the app to complete your email verification.
                
                This verification code will expire in 24 hours.
                
                If you didn't create an account, please ignore this email.
                
                Best regards,
                Todo App Team
            """.trimIndent()
        }
        javaMailSender.send(message)
    }

    fun sendPasswordResetEmail(email: String, token: String, firstName: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            setFrom(fromEmail)
            subject = "Reset Your Password"
            text = """
                Hi $firstName,
                
                We received a request to reset your password for your Todo App account.
                
                Please use the following 6-digit reset code to reset your password:
                
                RESET CODE: $token
                
                Enter this code in the app to proceed with password reset.
                
                This reset code will expire in 1 hour.
                
                If you didn't request a password reset, please ignore this email.
                
                Best regards,
                Todo App Team
            """.trimIndent()
        }
        javaMailSender.send(message)
    }

    fun sendPasswordChangeNotification(email: String, firstName: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            setFrom(fromEmail)
            subject = "Password Changed Successfully"
            text = """
                Hi $firstName,
                
                Your password has been successfully changed for your Todo App account.
                
                Change Details:
                - Date: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
                - Account: $email
                
                If you didn't make this change, please contact our support team immediately at:
                - Support URL: $frontendUrl/support
                - Or reply to this email for assistance
                
                For your security, we recommend:
                1. Using a strong, unique password
                2. Enabling two-factor authentication if available
                3. Regularly updating your password
                
                Best regards,
                Todo App Team
            """.trimIndent()
        }
        javaMailSender.send(message)
    }

    fun sendWelcomeEmail(email: String, firstName: String) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            setFrom(fromEmail)
            subject = "Welcome to Todo App!"
            text = """
                Hi $firstName,
                
                Welcome to Todo App! Your email has been successfully verified.
                
                You can now access all features of our application:
                - Create and manage your todo lists
                - Set reminders and due dates
                - Organize tasks by categories
                - Track your productivity
                
                Get started: $frontendUrl/dashboard
                
                If you have any questions or need help, feel free to contact our support team.
                
                Best regards,
                Todo App Team
            """.trimIndent()
        }
        javaMailSender.send(message)
    }

    fun sendAccountLockedNotification(email: String, firstName: String, lockoutMinutes: Long) {
        val message = SimpleMailMessage().apply {
            setTo(email)
            setFrom(fromEmail)
            subject = "Account Temporarily Locked - Security Alert"
            text = """
                Hi $firstName,
                
                Your Todo App account has been temporarily locked due to multiple failed login attempts.
                
                Security Details:
                - Account: $email
                - Locked at: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
                - Unlock time: Approximately $lockoutMinutes minutes from now
                
                If this wasn't you, please:
                1. Change your password immediately after the lockout period
                2. Review your account activity
                3. Contact our support team if you suspect unauthorized access
                
                Support: $frontendUrl/support
                
                Best regards,
                Todo App Team
            """.trimIndent()
        }
        javaMailSender.send(message)
    }
}