package com.arcadia.trivora

import retrofit2.Response
import java.io.IOException

class QuestionRepository {

    suspend fun getCategories(): Result<List<String>> {
        return try {
            val response: Response<ApiResponse<List<String>>> = RetrofitClient.instance.getCategories()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Result.Success(apiResponse.data ?: emptyList())
                } else {
                    Result.Failure(Exception(apiResponse?.message ?: "Failed to fetch categories"))
                }
            } else {
                Result.Failure(Exception("HTTP error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.Failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.Failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun getQuestionsByCategory(category: String): Result<List<Question>> {
        return try {
            val response: Response<ApiResponse<List<Question>>> = RetrofitClient.instance.getQuestionsByCategory(category)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Result.Success(apiResponse.data ?: emptyList())
                } else {
                    Result.Failure(Exception(apiResponse?.message ?: "Failed to fetch questions"))
                }
            } else {
                Result.Failure(Exception("HTTP error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.Failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.Failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}