package com.todo.data.repository

import com.todo.data.models.Task
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : MongoRepository<Task, String> {
    fun findByUserId(userId: String): List<Task>
    fun findByUserIdAndCompleted(userId: String, completed: Boolean): List<Task>
    fun findByIdAndUserId(id: String, userId: String): Task?
    fun deleteByIdAndUserId(id: String, userId: String)


    @Query("{'userId': ?0, '\$or': [{'title': {\$regex: ?1, \$options: 'i'}}, {'description': {\$regex: ?1, \$options: 'i'}}]}")
    fun findByUserIdAndTitleOrDescriptionContainingIgnoreCase(userId: String, searchTerm: String): List<Task>

    @Query("{'userId': ?0, 'completed': ?1, '\$or': [{'title': {\$regex: ?2, \$options: 'i'}}, {'description': {\$regex: ?2, \$options: 'i'}}]}")
    fun findByUserIdAndCompletedAndTitleOrDescriptionContainingIgnoreCase(userId: String, completed: Boolean, searchTerm: String): List<Task>

    fun findByUserIdAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(userId: String, titleKeyword: String, descriptionKeyword: String): List<Task>
}
