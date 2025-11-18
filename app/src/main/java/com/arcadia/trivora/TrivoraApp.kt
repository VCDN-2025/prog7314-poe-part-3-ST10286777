package com.arcadia.trivora

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrivoraApp: Application()
{
    // Database instance
    private val database by lazy { TrivoraDatabase.getInstance(this) }

    // Repository instance
    val offlineQuizRepository by lazy { OfflineQuizRepository(database) }



    override fun onCreate() {
        super.onCreate()
        SharedPrefs.init(this)

        // Initialize database and sync status
        initializeDatabase()

        // Start sync service
        if (NetworkUtils.isOnline(this)) {
            SyncService.startSyncService(this)
        }
    }

    private fun initializeDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            // Initialize sync status if it doesn't exist
            offlineQuizRepository.initializeSyncStatus()

            // Clean up old quizzes (runs once on app start)
            offlineQuizRepository.cleanupOldQuizzes()
        }
    }
}