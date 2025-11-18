package com.arcadia.trivora
import android.content.Context
import android.provider.Settings
import com.arcadia.trivora.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ScoreManager(private val context: Context) {

    private val apiService = RetrofitClient.instance
    private val deviceId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "unknown_device"

    fun recordQuizCompletion(
        userId: String,
        category: String,
        difficulty: String,
        score: Int,
        totalQuestions: Int,
        correctAnswers: Int,
        timeSpent: Long,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val quizResultRequest = QuizResultRequest(
                    category = category,
                    difficulty = difficulty,
                    score = score,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctAnswers,
                    timeSpent = timeSpent,
                    deviceId = deviceId
                )

                val response = apiService.saveQuizResult(quizResultRequest)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Successfully saved to backend
                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess?.invoke()
                    }
                    println("Quiz results saved successfully: ${response.body()?.data?.quizId}")
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to save quiz results"
                    CoroutineScope(Dispatchers.Main).launch {
                        onError?.invoke(errorMessage)
                    }
                    println("Failed to save quiz results: $errorMessage")
                }
            } catch (e: Exception) {
                val errorMessage = "Network error: ${e.message}"
                CoroutineScope(Dispatchers.Main).launch {
                    onError?.invoke(errorMessage)
                }
                println("Network error saving quiz results: ${e.message}")
            }
        }
    }

    suspend fun getUserStats(): UserStatsResponse? {
        return try {
            val response = apiService.getUserStats()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getQuizHistory(limit: Int = 10): List<QuizResult> {
        return try {
            val response = apiService.getQuizHistory(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}