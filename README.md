# Todo Backend API

A RESTful API for a todo application built with Kotlin, Spring Boot, and MongoDB. This API provides user authentication, task management, and email notifications.

## Features

- **User Authentication**
  - JWT-based authentication
  - Account registration with email verification
  - Login with account lockout after multiple failed attempts
  - Password reset functionality
  - Token refresh mechanism
  - Secure logout

- **Task Management**
  - Create, read, update, and delete tasks
  - Mark tasks as complete/incomplete
  - User-specific task isolation

- **Security**
  - Password encryption with BCrypt
  - JWT token authentication
  - Account lockout after failed login attempts
  - Email verification
  - CORS support for frontend integration

- **Email Notifications**
  - Email verification
  - Password reset

- **Database**
  - MongoDB integration
  - Environment-specific configurations

## Prerequisites

- Java 21 (JDK 21)
- MongoDB 4.4+
- Gradle 8.0+
- SMTP server access for email functionality

## Setup and Installation

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/todo-backend.git
cd todo-backend
```

### 2. Configure MongoDB

Ensure MongoDB is installed and running on your system:

```bash
# Start MongoDB service
sudo systemctl start mongod

# Verify MongoDB is running
sudo systemctl status mongod
```

The application will connect to MongoDB at `mongodb://localhost:27017/todoapp` by default.

### 3. Configure Email Service

The application uses Gmail SMTP for sending emails. Update the email configuration in `application.yml` with your credentials:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

**Note:** For Gmail, you need to use an App Password instead of your regular password. [Learn how to create an App Password](https://support.google.com/accounts/answer/185833).

### 4. Configure Application Properties

Review and update the following configuration files as needed:

- `application.yml` - Default configuration
- `application-dev.yml` - Development environment configuration
- `application-prod.yml` - Production environment configuration

Key configurations to review:
- MongoDB connection URI
- JWT secret and expiration times
- Frontend URL for CORS
- Email settings

### 5. Build and Run the Application

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

For development mode:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

For production mode:
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

The application will start on port 8080 by default.

## API Endpoints

### Authentication Endpoints

- `POST /api/auth/register` - Register a new user
  - Request: `{ "email": "user@example.com", "username": "user", "password": "password", "firstName": "John", "lastName": "Doe" }`

- `POST /api/auth/login` - Login user
  - Request: `{ "email": "user@example.com", "password": "password" }`

- `POST /api/auth/verify-email` - Verify email with token
  - Request: `{ "token": "123456" }`

- `POST /api/auth/resend-verification` - Resend verification email
  - Request: `{ "email": "user@example.com" }`

- `POST /api/auth/forgot-password` - Request password reset
  - Request: `{ "email": "user@example.com" }`

- `POST /api/auth/reset-password` - Reset password with token
  - Request: `{ "token": "123456", "newPassword": "newpassword" }`

- `POST /api/auth/refresh-token` - Refresh JWT token
  - Request: `{ "refreshToken": "refresh-token-value" }`

- `POST /api/auth/logout` - Logout user
  - Request: `{ "refreshToken": "refresh-token-value" }`

### Task Endpoints

- `GET /api/tasks` - Get all tasks for the authenticated user

- `POST /api/tasks` - Create a new task
  - Request: `{ "title": "Task title", "description": "Task description" }`

- `GET /api/tasks/{taskId}` - Get a specific task by ID

- `PUT /api/tasks/{taskId}` - Update a task
  - Request: `{ "title": "Updated title", "description": "Updated description", "completed": true }`

- `PATCH /api/tasks/{taskId}/toggle-completion` - Toggle task completion status

- `DELETE /api/tasks/{taskId}` - Delete a task

## Testing

Run the tests using Gradle:

```bash
./gradlew test
```

The application uses embedded MongoDB for testing, so no external MongoDB instance is required for running tests.

## Environment Variables

For production deployment, you can use environment variables to configure the application:

- `MONGODB_URI` - MongoDB connection URI
- `JWT_SECRET` - Secret key for JWT token generation
- `JWT_EXPIRATION` - JWT token expiration time in milliseconds

## Docker Support

The application can be containerized using Docker:

```bash
# Build Docker image
docker build -t todo-backend .

# Run Docker container
docker run -p 8080:8080 todo-backend
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
