package com.todo.data.repository

import com.todo.data.models.Task
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : MongoRepository<Task, String> {
    fun findByUserId(userId: String): List<Task>
    fun findByUserIdAndCompleted(userId: String, completed: Boolean): List<Task>
    fun findByIdAndUserId(id: String, userId: String): Task?
    fun deleteByIdAndUserId(id: String, userId: String)
}