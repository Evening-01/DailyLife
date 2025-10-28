package com.evening.dailylife.core.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.evening.dailylife.core.data.local.dao.TransactionDao
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import java.util.concurrent.Executors

@Database(entities = [TransactionEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val databaseExecutor by lazy {
            Executors.newFixedThreadPool(
                maxOf(2, Runtime.getRuntime().availableProcessors() / 2)
            )
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_life_database"
                )
                    .setQueryExecutor(databaseExecutor)
                    .setTransactionExecutor(databaseExecutor)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
