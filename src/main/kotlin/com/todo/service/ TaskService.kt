package com.todo.service

import com.todo.data.models.Task
import com.todo.data.repository.TaskRepository
import com.todo.data.repository.UserRepository
import com.todo.dto.task.*
import com.todo.exception.UserNotFoundException
import com.todo.exception.TaskNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {

    fun getAllTasks(userEmail: String): TasksResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException("User not found")

        val tasks = taskRepository.findByUserId(user.id!!)
            .map { it.toResponse() }

        return TasksResponse(
            message = if (tasks.isEmpty())
                "📝 No tasks found! Ready to add your first task?"
            else
                "✅ Successfully retrieved ${tasks.size} task${if (tasks.size > 1) "s" else ""}!",
            success = true,
            tasks = tasks,
            totalTasks = tasks.size
        )
    }

    fun createTask(request: CreateTaskRequest, userEmail: String): TaskCreatedResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException("User not found")

        val task = Task(
            title = request.title,
            description = request.description,
            userId = user.id!!
        )

        val savedTask = taskRepository.save(task)

        return TaskCreatedResponse(
            message = "🎉 Task '${savedTask.title}' has been successfully created! Time to get things done!",
            success = true,
            task = savedTask.toResponse()
        )
    }

    fun updateTask(taskId: String, request: UpdateTaskRequest, userEmail: String): TaskUpdatedResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException("User not found")

        val task = taskRepository.findByIdAndUserId(taskId, user.id!!)
            ?: throw TaskNotFoundException("Task not found")

        val updatedTask = task.copy(
            title = request.title ?: task.title,
            description = request.description ?: task.description,
            completed = request.completed ?: task.completed,
            updatedAt = LocalDateTime.now()
        )

        val savedTask = taskRepository.save(updatedTask)

        return TaskUpdatedResponse(
            message = "✏️ Task '${savedTask.title}' has been successfully updated!",
            success = true,
            task = savedTask.toResponse()
        )
    }

    fun toggleTaskCompletion(taskId: String, userEmail: String): TaskUpdatedResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException("User not found")

        val task = taskRepository.findByIdAndUserId(taskId, user.id!!)
            ?: throw TaskNotFoundException("Task not found")

        val updatedTask = task.copy(
            completed = !task.completed,
            updatedAt = LocalDateTime.now()
        )

        val savedTask = taskRepository.save(updatedTask)

        val statusMessage = if (savedTask.completed) {
            "🎯 Great job! Task '${savedTask.title}' has been marked as completed!"
        } else {
            "📝 Task '${savedTask.title}' has been marked as incomplete. Keep going!"
        }

        return TaskUpdatedResponse(
            message = statusMessage,
            success = true,
            task = savedTask.toResponse()
        )
    }

    fun deleteTask(taskId: String, userEmail: String): TaskDeletedResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException("User not found")

        val task = taskRepository.findByIdAndUserId(taskId, user.id!!)
            ?: throw TaskNotFoundException("Task not found")

        taskRepository.deleteByIdAndUserId(taskId, user.id!!)

        return TaskDeletedResponse(
            message = "🗑️ Task '${task.title}' has been successfully deleted!",
            success = true
        )
    }

    fun getTaskById(taskId: String, userEmail: String): TaskResponse {
        val user = userRepository.findByEmail(userEmail)
            ?: throw UserNotFoundException("User not found")

        val task = taskRepository.findByIdAndUserId(taskId, user.id!!)
            ?: throw TaskNotFoundException("Task not found")

        return task.toResponse()
    }

    private fun Task.toResponse(): TaskResponse {
        return TaskResponse(
            id = this.id!!,
            title = this.title,
            description = this.description,
            completed = this.completed,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}