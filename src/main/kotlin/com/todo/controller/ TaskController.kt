package com.todo.controller

import com.todo.dto.task.*
import com.todo.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping
    fun getAllTasks(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) completed: Boolean?,
        authentication: Authentication
    ): ResponseEntity<TasksResponse> {
        val tasksResponse = when {
            !search.isNullOrBlank() -> taskService.searchTasks(authentication.name, search, completed)
            completed != null -> taskService.getTasksByStatus(authentication.name, completed)
            else -> taskService.getAllTasks(authentication.name)
        }
        return ResponseEntity.ok(tasksResponse)
    }

    @GetMapping("/search")
    fun searchTasks(
        @RequestParam query: String,
        @RequestParam(required = false) completed: Boolean?,
        authentication: Authentication
    ): ResponseEntity<TasksResponse> {
        val tasksResponse = taskService.searchTasks(authentication.name, query, completed)
        return ResponseEntity.ok(tasksResponse)
    }

    @GetMapping("/filter")
    fun getTasksByStatus(
        @RequestParam completed: Boolean,
        authentication: Authentication
    ): ResponseEntity<TasksResponse> {
        val tasksResponse = taskService.getTasksByStatus(authentication.name, completed)
        return ResponseEntity.ok(tasksResponse)
    }

    @PostMapping
    fun createTask(
        @Valid @RequestBody request: CreateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskCreatedResponse> {
        val taskResponse = taskService.createTask(request, authentication.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse)
    }

    @GetMapping("/{taskId}")
    fun getTaskById(
        @PathVariable taskId: String,
        authentication: Authentication
    ): ResponseEntity<TaskResponse> {
        val task = taskService.getTaskById(taskId, authentication.name)
        return ResponseEntity.ok(task)
    }

    @PutMapping("/{taskId}")
    fun updateTask(
        @PathVariable taskId: String,
        @Valid @RequestBody request: UpdateTaskRequest,
        authentication: Authentication
    ): ResponseEntity<TaskUpdatedResponse> {
        val taskResponse = taskService.updateTask(taskId, request, authentication.name)
        return ResponseEntity.ok(taskResponse)
    }

    @PatchMapping("/{taskId}/toggle-completion")
    fun toggleTaskCompletion(
        @PathVariable taskId: String,
        authentication: Authentication
    ): ResponseEntity<TaskUpdatedResponse> {
        val taskResponse = taskService.toggleTaskCompletion(taskId, authentication.name)
        return ResponseEntity.ok(taskResponse)
    }

    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @PathVariable taskId: String,
        authentication: Authentication
    ): ResponseEntity<TaskDeletedResponse> {
        val deleteResponse = taskService.deleteTask(taskId, authentication.name)
        return ResponseEntity.ok(deleteResponse)
    }
}