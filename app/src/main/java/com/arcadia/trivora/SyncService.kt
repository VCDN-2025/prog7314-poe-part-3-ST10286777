package com.arcadia.trivora

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SyncService : LifecycleService() {

    companion object {
        private const val SYNC_INTERVAL = 30 * 60 * 1000L // 30 minutes
        fun startSyncService(context: Context) {
            val intent = Intent(context, SyncService::class.java)
            context.startService(intent)
        }

        fun stopSyncService(context: Context) {
            val intent = Intent(context, SyncService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startPeriodicSync()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // Immediate sync when service starts
        lifecycleScope.launch {
            syncData()
        }
        return START_STICKY
    }

    private fun startPeriodicSync() {
        lifecycleScope.launch {
            while (true) {
                if (NetworkUtils.isOnline(this@SyncService)) {
                    syncData()
                }
                delay(SYNC_INTERVAL)
            }
        }
    }

    private suspend fun syncData() {
        try {
            val database = TrivoraDatabase.getInstance(this)
            val repository = OfflineQuizRepository(database)

            // Sync unsynced quiz results (complete quizzes)
            val unsyncedResults = repository.getUnsyncedQuizResults()

            Log.d("SyncService", "Found ${unsyncedResults.size} unsynced quiz results")

            for (result in unsyncedResults) {
                try {
                    // Convert to API request format
                    val quizResultRequest = QuizResultRequest(
                        category = result.category,
                        difficulty = result.difficulty,
                        score = result.score,
                        totalQuestions = result.totalQuestions,
                        correctAnswers = result.correctAnswers,
                        timeSpent = result.timeSpent,
                        deviceId = result.deviceId
                    )

                    // Send to backend
                    val response = RetrofitClient.instance.saveQuizResult(quizResultRequest)

                    if (response.isSuccessful && response.body()?.success == true) {
                        // Mark as synced in local database
                        repository.markQuizResultSynced(result.id)
                        Log.d("SyncService", "Successfully synced quiz result: ${result.id}")
                    } else {
                        Log.e("SyncService", "Failed to sync quiz result ${result.id}: ${response.body()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e("SyncService", "Error syncing quiz result ${result.id}: ${e.message}")
                }
            }

            // Update sync status based on quiz results
            val pendingCount = repository.getUnsyncedQuizResults().size
            repository.updateSyncStatus(System.currentTimeMillis(), pendingCount)

            Log.d("SyncService", "Sync completed. Pending results: $pendingCount")

        } catch (e: Exception) {
            Log.e("SyncService", "Sync failed: ${e.message}")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}