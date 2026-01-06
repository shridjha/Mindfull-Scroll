package com.example.enddoomscroll.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.enddoomscroll.data.dao.AppSettingsDao
import com.example.enddoomscroll.data.dao.AppUsageDao
import com.example.enddoomscroll.data.dao.ChallengeHistoryDao
import com.example.enddoomscroll.data.entity.AppSettings
import com.example.enddoomscroll.data.entity.AppUsage
import com.example.enddoomscroll.data.entity.ChallengeHistory

@Database(
    entities = [AppUsage::class, AppSettings::class, ChallengeHistory::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun challengeHistoryDao(): ChallengeHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
        
        const val DATABASE_NAME = "mindfulscroll_db"
    }
}

