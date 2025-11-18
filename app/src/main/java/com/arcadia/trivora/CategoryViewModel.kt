package com.arcadia.trivora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class CategoryViewModel: ViewModel() {
    private val repository = QuestionRepository()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getCategories()) {
                is Result.Success -> {
                    _categories.value = result.data
                }
                is Result.Failure -> {
                    _error.value = result.exception.message ?: "Unknown error occurred"
                }
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}