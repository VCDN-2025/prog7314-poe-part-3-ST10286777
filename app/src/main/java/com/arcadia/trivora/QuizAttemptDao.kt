package com.arcadia.trivora

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {

    // Insert a new quiz attempt
    @Insert
    suspend fun insertAttempt(attempt: QuizAttempt): Long

    // Get all unsynced attempts
    @Query("SELECT * FROM quiz_attempts WHERE isSynced = 0 ORDER BY completedAt ASC")
    suspend fun getUnsyncedAttempts(): List<QuizAttempt>

    @Query("UPDATE quiz_attempts SET isSynced = 0 WHERE id = :attemptId")
    suspend fun markAttemptUnsynced(attemptId: Long)

    // Mark attempt as synced
    @Query("UPDATE quiz_attempts SET isSynced = 1 WHERE id = :attemptId")
    suspend fun markAttemptSynced(attemptId: Long)

    // Get all attempts (for history)
    @Query("SELECT * FROM quiz_attempts ORDER BY completedAt DESC")
    fun getAllAttempts(): Flow<List<QuizAttempt>>

    // Get attempts for a specific quiz
    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId")
    fun getAttemptsForQuiz(quizId: String): Flow<List<QuizAttempt>>

    // Get statistics
    @Query("SELECT COUNT(*) FROM quiz_attempts WHERE isCorrect = 1")
    suspend fun getCorrectAttemptsCount(): Int

    @Query("SELECT COUNT(*) FROM quiz_attempts")
    suspend fun getTotalAttemptsCount(): Int

    // Clear all attempts
    @Query("DELETE FROM quiz_attempts")
    suspend fun clearAllAttempts()
}