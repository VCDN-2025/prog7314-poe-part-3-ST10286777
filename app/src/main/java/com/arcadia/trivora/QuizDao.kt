package com.arcadia.trivora

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    // Get all available quizzes (not completed)
    @Query("SELECT * FROM local_quizzes WHERE isCompleted = 0 ORDER BY downloadedAt DESC")
    fun getAvailableQuizzes(): Flow<List<LocalQuiz>>

    // Get quizzes by category
    @Query("SELECT * FROM local_quizzes WHERE category = :category AND isCompleted = 0")
    fun getQuizzesByCategory(category: String): Flow<List<LocalQuiz>>

    // Get a specific quiz
    @Query("SELECT * FROM local_quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: String): LocalQuiz?

    // Insert or update quizzes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<LocalQuiz>)

    // Mark a quiz as completed
    @Query("UPDATE local_quizzes SET isCompleted = 1 WHERE id = :quizId")
    suspend fun markQuizCompleted(quizId: String)

    // Get count of available quizzes
    @Query("SELECT COUNT(*) FROM local_quizzes WHERE isCompleted = 0")
    suspend fun getAvailableQuizCount(): Int

    // Delete old quizzes (older than 30 days)
    @Query("DELETE FROM local_quizzes WHERE downloadedAt < :timestamp")
    suspend fun deleteOldQuizzes(timestamp: Long)

    // Clear all local quizzes
    @Query("DELETE FROM local_quizzes")
    suspend fun clearAllQuizzes()
}