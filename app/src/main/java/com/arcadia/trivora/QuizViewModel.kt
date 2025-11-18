package com.arcadia.trivora

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class QuizState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val score: Int = 0,
    val isAnswerSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val quizCompleted: Boolean = false,
    val isSavingResults: Boolean = false,
    val saveResultError: String? = null,
    val isOfflineMode: Boolean = false
)

class QuizViewModel : ViewModel() {
    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    private val apiService = RetrofitClient.instance
    private lateinit var scoreManager: ScoreManager
    private lateinit var offlineQuizRepository: OfflineQuizRepository
    private var isOnline: Boolean = true

    // Tracks quiz metadata
    private var quizStartTime: Long = 0
    private var currentCategory: String = ""
    private var currentDifficulty: String = "Medium"
    private var userId: String = ""
    private lateinit var deviceId: String

    fun initialize(context: Context, category: String = "General", difficulty: String = "Medium") {
        scoreManager = ScoreManager(context)
        val database = TrivoraDatabase.getInstance(context)
        offlineQuizRepository = OfflineQuizRepository(database)

        quizStartTime = System.currentTimeMillis()
        currentCategory = category
        currentDifficulty = difficulty

        // Get user ID from SharedPrefs or Firebase Auth
        userId = SharedPrefs.getUserEmail() ?:
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

        // Get persistent device ID
        deviceId = DeviceIdManager.getDeviceId(context)

        // Check network availability
        checkNetworkStatus(context)
    }

    private fun checkNetworkStatus(context: Context) {
        isOnline = NetworkUtils.isOnline(context)
        _quizState.value = _quizState.value.copy(isOfflineMode = !isOnline)
    }

    fun loadQuestions(category: String) {
        _quizState.value = _quizState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                if (isOnline) {
                    // Online mode - fetch from API
                    val response = apiService.getQuestionsByCategory(category)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val questions = response.body()?.data ?: emptyList()

                        // Store questions for offline use
                        saveQuestionsForOffline(questions)

                        _quizState.value = _quizState.value.copy(
                            questions = questions,
                            isLoading = false,
                            currentQuestionIndex = 0,
                            isOfflineMode = false
                        )
                    } else {
                        // If online fails, try offline
                        loadQuestionsFromOffline(category)
                    }
                } else {
                    // Offline mode - load from local database
                    loadQuestionsFromOffline(category)
                }
            } catch (e: Exception) {
                // If network error, try offline
                loadQuestionsFromOffline(category)
            }
        }
    }

    private suspend fun loadQuestionsFromOffline(category: String) {
        try {
            val localQuizzes = offlineQuizRepository.getQuizzesByCategory(category).first()
            val questions = localQuizzes.map { localQuiz ->
                Question(
                    questionId = localQuiz.id,
                    category = localQuiz.category,
                    questionText = localQuiz.question,
                    choices = localQuiz.options,
                    answer = localQuiz.correctAnswer,
                    difficulty = localQuiz.difficulty
                )
            }

            if (questions.isNotEmpty()) {
                _quizState.value = _quizState.value.copy(
                    questions = questions,
                    isLoading = false,
                    currentQuestionIndex = 0,
                    isOfflineMode = true
                )
            } else {
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    error = "No questions available offline. Please connect to internet to download questions."
                )
            }
        } catch (e: Exception) {
            _quizState.value = _quizState.value.copy(
                isLoading = false,
                error = "Failed to load offline questions: ${e.message}"
            )
        }
    }

    private suspend fun saveQuestionsForOffline(questions: List<Question>) {
        try {
            val localQuizzes = questions.map { question ->
                LocalQuiz(
                    id = question.questionId,
                    question = question.questionText,
                    options = question.choices,
                    correctAnswer = question.answer,
                    category = question.category,
                    difficulty = question.difficulty,
                    explanation = null,
                    downloadedAt = System.currentTimeMillis(),
                    isCompleted = false
                )
            }
            offlineQuizRepository.insertQuizzes(localQuizzes)
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Failed to save questions offline: ${e.message}")
        }
    }

    fun selectAnswer(answer: String) {
        _quizState.value = _quizState.value.copy(selectedAnswer = answer)
    }

    fun submitAnswer() {
        val currentState = _quizState.value
        val currentQuestion = currentState.questions.getOrNull(currentState.currentQuestionIndex) ?: return

        val isCorrect = currentState.selectedAnswer == currentQuestion.answer
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        _quizState.value = currentState.copy(
            isAnswerSubmitted = true,
            score = newScore
        )

        // Save attempt for local tracking (optional)
        saveCurrentAttempt(isCorrect)
    }

    private fun saveCurrentAttempt(isCorrect: Boolean) {
        val currentState = _quizState.value
        val currentQuestion = currentState.questions.getOrNull(currentState.currentQuestionIndex) ?: return

        val timeSpent = (System.currentTimeMillis() - quizStartTime) / 1000

        viewModelScope.launch {
            try {
                val attempt = QuizAttempt(
                    quizId = currentQuestion.questionId,
                    selectedAnswer = currentState.selectedAnswer ?: "",
                    isCorrect = isCorrect,
                    timeSpent = timeSpent.toInt(),
                    completedAt = System.currentTimeMillis(),
                    isSynced = !_quizState.value.isOfflineMode
                )

                offlineQuizRepository.saveQuizAttempt(attempt)
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Failed to save attempt: ${e.message}")
            }
        }
    }

    fun nextQuestion() {
        val currentState = _quizState.value
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex >= currentState.questions.size) {
            saveQuizResults(currentState)
            _quizState.value = currentState.copy(quizCompleted = true)
        } else {
            _quizState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer = null,
                isAnswerSubmitted = false
            )
        }
    }

    private fun saveQuizResults(state: QuizState) {
        val timeSpent = (System.currentTimeMillis() - quizStartTime) / 1000

        _quizState.value = state.copy(isSavingResults = true, saveResultError = null)

        viewModelScope.launch {
            try {
                // Generate a unique quiz ID for this session
                val quizSessionId = UUID.randomUUID().toString()

                // Create local quiz result
                val localQuizResult = LocalQuizResult(
                    quizId = quizSessionId,
                    userId = userId,
                    category = currentCategory,
                    difficulty = currentDifficulty,
                    score = state.score,
                    totalQuestions = state.questions.size,
                    correctAnswers = state.score,
                    timeSpent = timeSpent,
                    completedAt = System.currentTimeMillis(),
                    deviceId = deviceId,
                    isSynced = !_quizState.value.isOfflineMode
                )

                // Save to local database
                val resultId = offlineQuizRepository.saveQuizResult(localQuizResult)

                // If online, try to sync immediately to backend
                if (!_quizState.value.isOfflineMode) {
                    try {
                        val quizResultRequest = QuizResultRequest(
                            category = currentCategory,
                            difficulty = currentDifficulty,
                            score = state.score,
                            totalQuestions = state.questions.size,
                            correctAnswers = state.score,
                            timeSpent = timeSpent,
                            deviceId = deviceId
                        )

                        val response = apiService.saveQuizResult(quizResultRequest)
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Mark as synced in local database
                            offlineQuizRepository.markQuizResultSynced(resultId)
                            Log.d("QuizViewModel", "Quiz result saved to backend successfully")
                        } else {
                            Log.e("QuizViewModel", "Failed to save quiz result to backend: ${response.body()?.message}")
                            // Result remains in local DB with isSynced = false, will sync later
                        }
                    } catch (e: Exception) {
                        Log.e("QuizViewModel", "Error saving quiz result to backend: ${e.message}")
                        // Result remains in local DB with isSynced = false, will sync later
                    }
                }

                // Mark all questions in this quiz as completed
                state.questions.forEach { question ->
                    offlineQuizRepository.markQuizCompleted(question.questionId)
                }

                _quizState.value = _quizState.value.copy(isSavingResults = false)

            } catch (e: Exception) {
                _quizState.value = _quizState.value.copy(
                    isSavingResults = false,
                    saveResultError = "Failed to save results: ${e.message}"
                )
            }
        }
    }

    fun setQuestions(questions: List<Question>) {
        _quizState.value = _quizState.value.copy(
            questions = questions,
            isLoading = false,
            currentQuestionIndex = 0,
            score = 0
        )

        // Save these questions for offline use
        viewModelScope.launch {
            saveQuestionsForOffline(questions)
        }
    }

    fun retryQuiz() {
        _quizState.value = QuizState(
            questions = _quizState.value.questions,
            currentQuestionIndex = 0,
            score = 0,
            isOfflineMode = _quizState.value.isOfflineMode
        )
        quizStartTime = System.currentTimeMillis()
    }

    fun clearSaveError() {
        _quizState.value = _quizState.value.copy(saveResultError = null)
    }

    // Method to download questions for offline use
    fun downloadQuestionsForOffline(category: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getQuestionsByCategory(category)
                if (response.isSuccessful && response.body()?.success == true) {
                    val questions = response.body()?.data ?: emptyList()
                    saveQuestionsForOffline(questions)
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Failed to download questions offline: ${e.message}")
            }
        }
    }
}