package com.arcadia.trivora

import kotlinx.coroutines.flow.Flow

class OfflineQuizRepository(private val database: TrivoraDatabase) {

    private val quizDao: QuizDao = database.quizDao()
    private val attemptDao: QuizAttemptDao = database.attemptDao()
    private val syncDao: SyncDao = database.syncDao()
    private val localQuizResultDao: LocalQuizResultDao = database.localQuizResultDao()

    // Quiz operations
    suspend fun insertQuizzes(quizzes: List<LocalQuiz>) {
        quizDao.insertQuizzes(quizzes)
    }

    fun getAvailableQuizzes(): Flow<List<LocalQuiz>> {
        return quizDao.getAvailableQuizzes()
    }

    fun getQuizzesByCategory(category: String): Flow<List<LocalQuiz>> {
        return quizDao.getQuizzesByCategory(category)
    }

    suspend fun getQuizById(quizId: String): LocalQuiz? {
        return quizDao.getQuizById(quizId)
    }

    suspend fun markQuizCompleted(quizId: String) {
        quizDao.markQuizCompleted(quizId)
    }

    suspend fun getAvailableQuizCount(): Int {
        return quizDao.getAvailableQuizCount()
    }

    // Attempt operations
    suspend fun saveQuizAttempt(attempt: QuizAttempt): Long {
        return attemptDao.insertAttempt(attempt)
    }

    suspend fun getUnsyncedAttempts(): List<QuizAttempt> {
        return attemptDao.getUnsyncedAttempts()
    }

    suspend fun markAttemptSynced(attemptId: Long) {
        attemptDao.markAttemptSynced(attemptId)
    }

    suspend fun markAttemptUnsynced(attemptId: Long) {
        attemptDao.markAttemptUnsynced(attemptId)
    }

    fun getAllAttempts(): Flow<List<QuizAttempt>> {
        return attemptDao.getAllAttempts()
    }

    fun getAttemptsForQuiz(quizId: String): Flow<List<QuizAttempt>> {
        return attemptDao.getAttemptsForQuiz(quizId)
    }

    suspend fun getQuizStatistics(): Pair<Int, Int> {
        val correct = attemptDao.getCorrectAttemptsCount()
        val total = attemptDao.getTotalAttemptsCount()
        return Pair(correct, total)
    }

    // Quiz Result operations
    suspend fun saveQuizResult(quizResult: LocalQuizResult): Long {
        return localQuizResultDao.insertQuizResult(quizResult)
    }

    suspend fun getUnsyncedQuizResults(): List<LocalQuizResult> {
        return localQuizResultDao.getUnsyncedResults()
    }

    suspend fun markQuizResultSynced(resultId: Long) {
        localQuizResultDao.markResultSynced(resultId)
    }

    fun getAllQuizResults(): Flow<List<LocalQuizResult>> {
        return localQuizResultDao.getAllResults()
    }

    fun getQuizResultsByCategory(category: String): Flow<List<LocalQuizResult>> {
        return localQuizResultDao.getResultsByCategory(category)
    }

    // Sync operations
    suspend fun getSyncStatus(): SyncStatus? {
        return syncDao.getSyncStatus()
    }

    suspend fun initializeSyncStatus() {
        val currentStatus = syncDao.getSyncStatus()
        if (currentStatus == null) {
            syncDao.insertSyncStatus(SyncStatus())
        }
    }

    suspend fun updateSyncStatus(lastSyncTime: Long, pendingCount: Int) {
        syncDao.updateSyncStatus(lastSyncTime, pendingCount)
    }

    suspend fun updatePendingCount(pendingCount: Int) {
        syncDao.updatePendingCount(pendingCount)
    }

    // Maintenance operations
    suspend fun cleanupOldQuizzes() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        quizDao.deleteOldQuizzes(thirtyDaysAgo)
    }

    suspend fun clearAllData() {
        quizDao.clearAllQuizzes()
        attemptDao.clearAllAttempts()
        localQuizResultDao.clearAllResults()
        syncDao.insertSyncStatus(SyncStatus())
    }

    // Add method to download questions for offline
    suspend fun downloadQuestionsForOffline(category: String) {

            val response = RetrofitClient.instance.getQuestionsByCategory(category)
            if (response.isSuccessful && response.body()?.success == true) {
                val questions = response.body()?.data ?: emptyList()
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
                insertQuizzes(localQuizzes)
            }

    }
}