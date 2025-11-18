
package com.arcadia.trivora

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/questions/random")
    suspend fun getRandomQuestion(): Response<ApiResponse<Question>>

    @GET("api/questions/random/{count}")
    suspend fun getRandomQuestions(@Path("count") count: Int): Response<ApiResponse<List<Question>>>

    @GET("api/users/profile")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>

    @GET("api/questions")
    suspend fun getQuestions(): Response<ApiResponse<List<Question>>>

    @GET("api/questions/categories")
    suspend fun getCategories(): Response<ApiResponse<List<String>>>

    @GET("api/questions/category/{category}")
    suspend fun getQuestionsByCategory(@Path("category") category: String): Response<ApiResponse<List<Question>>>

    @GET("api/users/profile")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfile>>

    @GET("api/users/stats")
    suspend fun getUserStats(): Response<ApiResponse<UserStatsResponse>>

    @PUT("api/users/profile/display-name")
    suspend fun updateDisplayName(@Body request: UpdateDisplayNameRequest): Response<ApiResponse<UserProfile>>

    @POST("api/quiz-results")
    suspend fun saveQuizResult(@Body request: QuizResultRequest): Response<ApiResponse<QuizResultResponse>>

    @GET("api/quiz-results/history")
    suspend fun getQuizHistory(@Query("limit") limit: Int = 10): Response<ApiResponse<List<QuizResult>>>

    @GET("api/quiz-results/category/{category}")
    suspend fun getQuizResultsByCategory(@Path("category") category: String): Response<ApiResponse<List<QuizResult>>>

    @PUT("api/users/fcm-token")
    suspend fun updateFCMToken(@Body request: UpdateFCMTokenRequest): Response<ApiResponse<Any>>

    @POST("api/notifications/send-test")
    suspend fun sendTestNotification(@Body request: TestNotificationRequest): Response<ApiResponse<Any>>
}

