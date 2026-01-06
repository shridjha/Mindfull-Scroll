package com.example.enddoomscroll.data.dao

import androidx.room.*
import com.example.enddoomscroll.data.entity.AppUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage WHERE date = :date AND packageName = :packageName")
    suspend fun getUsageForDate(date: Long, packageName: String): AppUsage?
    
    @Query("SELECT * FROM app_usage WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getUsageBetweenDates(startDate: Long, endDate: Long): Flow<List<AppUsage>>
    
    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND date = :date")
    fun getUsageForPackageAndDate(packageName: String, date: Long): Flow<AppUsage?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: AppUsage)
    
    @Update
    suspend fun updateUsage(usage: AppUsage)
    
    @Query("SELECT SUM(timeUsed) FROM app_usage WHERE date = :date")
    suspend fun getTotalUsageForDate(date: Long): Long?
    
    @Query("SELECT SUM(blocksTriggered) FROM app_usage WHERE date = :date")
    suspend fun getTotalBlocksForDate(date: Long): Int?
}
