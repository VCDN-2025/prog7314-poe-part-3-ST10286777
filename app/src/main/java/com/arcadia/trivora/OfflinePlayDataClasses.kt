package com.arcadia.trivora

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "local_quizzes")
data class LocalQuiz(
    @PrimaryKey
    val id: String,
    val question: String,

    @TypeConverters(ListConverter::class)
    val options: List<String>,

    val correctAnswer: String,
    val category: String,
    val difficulty: String,
    val explanation: String? = null,
    val downloadedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Entity(tableName = "local_quiz_results")
data class LocalQuizResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: String = "", // Generated UUID for the quiz session
    val userId: String,
    val category: String,
    val difficulty: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeSpent: Long, // in seconds
    val completedAt: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "quiz_attempts")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: String,
    val selectedAnswer: String,
    val isCorrect: Boolean,
    val timeSpent: Int, // in seconds
    val completedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "sync_status")
data class SyncStatus(
    @PrimaryKey
    val id: Int = 1, // Single row for global sync status
    val lastSyncTime: Long = 0,
    val pendingSyncCount: Int = 0
)