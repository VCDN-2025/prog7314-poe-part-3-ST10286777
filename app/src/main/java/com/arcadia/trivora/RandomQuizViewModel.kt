package com.arcadia.trivora

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RandomQuizViewModel : ViewModel() {
    private val apiService = RetrofitClient.instance

    private val _randomQuestion = MutableLiveData<Question?>()
    val randomQuestion: LiveData<Question?> = _randomQuestion

    private val _randomQuestions = MutableLiveData<List<Question>>()
    val randomQuestions: LiveData<List<Question>> = _randomQuestions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRandomQuestion() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = apiService.getRandomQuestion()
                if (response.isSuccessful && response.body()?.success == true) {
                    _randomQuestion.value = response.body()?.data
                } else {
                    _error.value = response.body()?.message ?: "Failed to load random question"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRandomQuestions(count: Int) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = apiService.getRandomQuestions(count)
                if (response.isSuccessful && response.body()?.success == true) {
                    _randomQuestions.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = response.body()?.message ?: "Failed to load random questions"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}