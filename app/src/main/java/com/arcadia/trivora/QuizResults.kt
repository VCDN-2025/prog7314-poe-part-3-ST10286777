package com.arcadia.trivora

data class QuizResult(
    val quizId: String,
    val userId: String,
    val category: String,
    val difficulty: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeSpent: Long,
    val date: String,
    val deviceId: String
)

data class QuizResultRequest(
    val category: String,
    val difficulty: String,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val timeSpent: Long,
    val deviceId: String
)

data class QuizResultResponse(
    val quizId: String,
    val userStats: UserStatsResponse
)



