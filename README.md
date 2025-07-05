# Todo Backend API

A RESTful API for a todo application built with Kotlin, Spring Boot, and MongoDB.

## Features

- User authentication with JWT
- CRUD operations for tasks
- MongoDB integration
- Input validation
- Error handling
- Docker support

## Getting Started

### Prerequisites

- Java 17+
- MongoDB
- Gradle

### Installation

1. Clone the repository
2. Start MongoDB
3. Run the application: `./gradlew bootRun`

### API Endpoints

- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login user
- `GET /api/tasks` - Get all tasks
- `POST /api/tasks` - Create task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

## Testing

Run tests: `./gradlew test`

