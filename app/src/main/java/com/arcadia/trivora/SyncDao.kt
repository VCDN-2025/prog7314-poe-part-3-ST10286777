package com.arcadia.trivora

import androidx.room.*

@Dao
interface SyncDao {

    @Query("SELECT * FROM sync_status WHERE id = 1")
    suspend fun getSyncStatus(): SyncStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncStatus(status: SyncStatus)

    @Query("UPDATE sync_status SET lastSyncTime = :timestamp, pendingSyncCount = :pendingCount WHERE id = 1")
    suspend fun updateSyncStatus(timestamp: Long, pendingCount: Int)

    @Query("UPDATE sync_status SET pendingSyncCount = :pendingCount WHERE id = 1")
    suspend fun updatePendingCount(pendingCount: Int)
}