package com.arcadia.trivora

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        LocalQuiz::class,
        QuizAttempt::class,
        SyncStatus::class,
        LocalQuizResult::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(ListConverter::class)
abstract class TrivoraDatabase : RoomDatabase() {

    abstract fun quizDao(): QuizDao
    abstract fun attemptDao(): QuizAttemptDao
    abstract fun syncDao(): SyncDao
    abstract fun localQuizResultDao(): LocalQuizResultDao

    companion object {
        @Volatile
        private var INSTANCE: TrivoraDatabase? = null

        fun getInstance(context: Context): TrivoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrivoraDatabase::class.java,
                    "trivora_database"
                ) .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}