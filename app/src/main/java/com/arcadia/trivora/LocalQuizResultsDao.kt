package com.arcadia.trivora

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalQuizResultDao {

    @Insert
    suspend fun insertQuizResult(quizResult: LocalQuizResult): Long

    // Get all unsynced quiz results
    @Query("SELECT * FROM local_quiz_results WHERE isSynced = 0 ORDER BY completedAt ASC")
    suspend fun getUnsyncedResults(): List<LocalQuizResult>

    // Mark quiz result as synced
    @Query("UPDATE local_quiz_results SET isSynced = 1 WHERE id = :resultId")
    suspend fun markResultSynced(resultId: Long)

    // Get all quiz results for history
    @Query("SELECT * FROM local_quiz_results ORDER BY completedAt DESC")
    fun getAllResults(): Flow<List<LocalQuizResult>>

    // Get results by category
    @Query("SELECT * FROM local_quiz_results WHERE category = :category ORDER BY completedAt DESC")
    fun getResultsByCategory(category: String): Flow<List<LocalQuizResult>>

    // Clear all results
    @Query("DELETE FROM local_quiz_results")
    suspend fun clearAllResults()
}