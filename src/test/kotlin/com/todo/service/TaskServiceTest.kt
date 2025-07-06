package com.todo.service

import com.todo.data.models.Task
import com.todo.data.models.User
import com.todo.data.repository.TaskRepository
import com.todo.data.repository.UserRepository
import com.todo.dto.task.*
import com.todo.exception.TaskNotFoundException
import com.todo.exception.UserNotFoundException
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*

import java.time.LocalDateTime

class TaskServiceTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var userRepository: UserRepository
    private lateinit var taskService: TaskService

    private val testUserEmail = "test@example.com"
    private val testUserId = "user123"
    private val testTaskId = "task123"
    private val testUser = User(
        id = testUserId,
        email = testUserEmail,
        username = "Test User",
        firstName = "tester",
        lastName = "king",
        password = "hashedPassword123"
    )
    private val testTask = Task(
        id = testTaskId,
        title = "Test Task",
        description = "Test Description",
        completed = false,
        userId = testUserId,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        userRepository = mockk()
        taskService = TaskService(taskRepository, userRepository)
    }

    @Test
    fun `getAllTasks should return tasks when user exists and has tasks`() {

        val tasks = listOf(testTask)
        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByUserId(testUserId) } returns tasks


        val result = taskService.getAllTasks(testUserEmail)


        assertTrue(result.success)
        assertEquals(1, result.tasks.size)
        assertEquals(1, result.totalTasks)
        assertEquals("✅ Successfully retrieved 1 task!", result.message)
        assertEquals(testTask.title, result.tasks[0].title)

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByUserId(testUserId) }
    }

    @Test
    fun `getAllTasks should return empty message when user has no tasks`() {

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByUserId(testUserId) } returns emptyList()


        val result = taskService.getAllTasks(testUserEmail)


        assertTrue(result.success)
        assertEquals(0, result.tasks.size)
        assertEquals(0, result.totalTasks)
        assertEquals("📝 No tasks found! Ready to add your first task?", result.message)
    }

    @Test
    fun `getAllTasks should throw UserNotFoundException when user doesn't exist`() {

        every { userRepository.findByEmail(testUserEmail) } returns null


        assertThrows<UserNotFoundException> {
            taskService.getAllTasks(testUserEmail)
        }

        verify { userRepository.findByEmail(testUserEmail) }
        verify(exactly = 0) { taskRepository.findByUserId(any()) }
    }

    @Test
    fun `createTask should create and return task when user exists`() {

        val request = CreateTaskRequest(
            title = "New Task",
            description = "New Description"
        )
        val savedTask = testTask.copy(title = request.title, description = request.description)

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.save(any()) } returns savedTask

        val result = taskService.createTask(request, testUserEmail)


        assertTrue(result.success)
        assertEquals("🎉 Task 'New Task' has been successfully created! Time to get things done!", result.message)
        assertEquals(savedTask.title, result.task.title)
        assertEquals(savedTask.description, result.task.description)

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.save(any()) }
    }

    @Test
    fun `createTask should throw UserNotFoundException when user doesn't exist`() {

        val request = CreateTaskRequest(title = "New Task", description = "New Description")
        every { userRepository.findByEmail(testUserEmail) } returns null


        assertThrows<UserNotFoundException> {
            taskService.createTask(request, testUserEmail)
        }

        verify { userRepository.findByEmail(testUserEmail) }
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `updateTask should update and return task when user and task exist`() {

        val request = UpdateTaskRequest(
            title = "Updated Task",
            description = "Updated Description",
            completed = true
        )
        val updatedTask = testTask.copy(
            title = request.title!!,
            description = request.description!!,
            completed = request.completed!!
        )

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns testTask
        every { taskRepository.save(any()) } returns updatedTask


        val result = taskService.updateTask(testTaskId, request, testUserEmail)

        assertTrue(result.success)
        assertEquals("✏️ Task 'Updated Task' has been successfully updated!", result.message)
        assertEquals(updatedTask.title, result.task.title)
        assertEquals(updatedTask.description, result.task.description)
        assertTrue(result.task.completed)

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
        verify { taskRepository.save(any()) }
    }

    @Test
    fun `updateTask should throw TaskNotFoundException when task doesn't exist`() {

        val request = UpdateTaskRequest(title = "Updated Task")
        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns null

        assertThrows<TaskNotFoundException> {
            taskService.updateTask(testTaskId, request, testUserEmail)
        }

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `toggleTaskCompletion should mark incomplete task as completed`() {

        val incompleteTask = testTask.copy(completed = false)
        val completedTask = testTask.copy(completed = true)

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns incompleteTask
        every { taskRepository.save(any()) } returns completedTask


        val result = taskService.toggleTaskCompletion(testTaskId, testUserEmail)

        assertTrue(result.success)
        assertEquals("🎯 Great job! Task 'Test Task' has been marked as completed!", result.message)
        assertTrue(result.task.completed)

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
        verify { taskRepository.save(any()) }
    }

    @Test
    fun `toggleTaskCompletion should mark completed task as incomplete`() {

        val completedTask = testTask.copy(completed = true)
        val incompleteTask = testTask.copy(completed = false)

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns completedTask
        every { taskRepository.save(any()) } returns incompleteTask


        val result = taskService.toggleTaskCompletion(testTaskId, testUserEmail)


        assertTrue(result.success)
        assertEquals("📝 Task 'Test Task' has been marked as incomplete. Keep going!", result.message)
        assertFalse(result.task.completed)

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
        verify { taskRepository.save(any()) }
    }

    @Test
    fun `deleteTask should delete task when user and task exist`() {

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns testTask
        every { taskRepository.deleteByIdAndUserId(testTaskId, testUserId) } just Runs

        val result = taskService.deleteTask(testTaskId, testUserEmail)

        assertTrue(result.success)
        assertEquals("🗑️ Task 'Test Task' has been successfully deleted!", result.message)

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
        verify { taskRepository.deleteByIdAndUserId(testTaskId, testUserId) }
    }

    @Test
    fun `deleteTask should throw TaskNotFoundException when task doesn't exist`() {

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns null


        assertThrows<TaskNotFoundException> {
            taskService.deleteTask(testTaskId, testUserEmail)
        }

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
        verify(exactly = 0) { taskRepository.deleteByIdAndUserId(any(), any()) }
    }

    @Test
    fun `getTaskById should return task when user and task exist`() {

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns testTask

        val result = taskService.getTaskById(testTaskId, testUserEmail)

        assertEquals(testTask.id, result.id)
        assertEquals(testTask.title, result.title)
        assertEquals(testTask.description, result.description)
        assertEquals(testTask.completed, result.completed)

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
    }

    @Test
    fun `getTaskById should throw TaskNotFoundException when task doesn't exist`() {

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns null


        assertThrows<TaskNotFoundException> {
            taskService.getTaskById(testTaskId, testUserEmail)
        }

        verify { userRepository.findByEmail(testUserEmail) }
        verify { taskRepository.findByIdAndUserId(testTaskId, testUserId) }
    }

    @Test
    fun `multiple tasks should return correct plural message`() {

        val tasks = listOf(testTask, testTask.copy(id = "task2"))
        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByUserId(testUserId) } returns tasks

        val result = taskService.getAllTasks(testUserEmail)

        assertTrue(result.success)
        assertEquals(2, result.tasks.size)
        assertEquals(2, result.totalTasks)
        assertEquals("✅ Successfully retrieved 2 tasks!", result.message)
    }

    @Test
    fun `updateTask should preserve existing values when request fields are null`() {

        val request = UpdateTaskRequest(
            title = "Updated Title",
            description = null,
            completed = null
        )
        val updatedTask = testTask.copy(title = request.title!!)

        every { userRepository.findByEmail(testUserEmail) } returns testUser
        every { taskRepository.findByIdAndUserId(testTaskId, testUserId) } returns testTask
        every { taskRepository.save(any()) } returns updatedTask


        val result = taskService.updateTask(testTaskId, request, testUserEmail)


        assertTrue(result.success)
        assertEquals("Updated Title", result.task.title)
        assertEquals(testTask.description, result.task.description) // preserved
        assertEquals(testTask.completed, result.task.completed) // preserved

        verify { taskRepository.save(match { task ->
            task.title == "Updated Title" &&
                    task.description == testTask.description &&
                    task.completed == testTask.completed
        }) }
    }
}

