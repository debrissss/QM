package com.lym.quietmind.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lym.quietmind.data.dao.EntertainmentDao
import com.lym.quietmind.data.dao.FocusSessionDao
import com.lym.quietmind.data.entity.EntertainmentRecordEntity
import com.lym.quietmind.data.entity.FocusSessionEntity

@Database(entities = [FocusSessionEntity::class, EntertainmentRecordEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun entertainmentDao(): EntertainmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `entertainment_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `startTime` INTEGER NOT NULL, `type` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quietmind_sandbox_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
