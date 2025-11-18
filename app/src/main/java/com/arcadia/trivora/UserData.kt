package com.arcadia.trivora

data class UserData(
    val userId: String? = null,
    val email: String,
    val displayName: String? = null,
    val totalScore: Int = 0,
    val totalQuizzes: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val averageScore: Double = 0.0,
    val bestCategory: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null

)
data class UserProfile(
    val user: UserData?
)
data class UpdateDisplayNameRequest(
    val displayName: String
)
data class UserStatsResponse(
    val totalScore: Int,
    val totalQuizzes: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val averageScore: Double,
    val bestCategory: String
)