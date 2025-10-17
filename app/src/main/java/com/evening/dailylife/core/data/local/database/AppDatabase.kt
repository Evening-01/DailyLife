package com.evening.dailylife.core.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.evening.dailylife.core.data.local.dao.TransactionDao
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import java.util.concurrent.Executors

@Database(entities = [TransactionEntity::class], version = 2, exportSchema = false)
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_transactions_isDeleted_date ON transactions(isDeleted, date)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_transactions_category ON transactions(category)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_transactions_mood ON transactions(mood)"
                )
            }
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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
