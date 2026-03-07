package com.lym.quietmind.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lym.quietmind.data.dao.FocusSessionDao
import com.lym.quietmind.data.entity.FocusSessionEntity

@Database(entities = [FocusSessionEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quietmind_sandbox_database"
                )
                .fallbackToDestructiveMigration() // Sandbox allow data obliteration on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
