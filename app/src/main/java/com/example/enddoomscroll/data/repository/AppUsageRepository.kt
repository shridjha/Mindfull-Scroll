package com.example.enddoomscroll.data.repository

import com.example.enddoomscroll.data.dao.AppSettingsDao
import com.example.enddoomscroll.data.dao.AppUsageDao
import com.example.enddoomscroll.data.dao.ChallengeHistoryDao
import com.example.enddoomscroll.data.entity.AppSettings
import com.example.enddoomscroll.data.entity.AppUsage
import com.example.enddoomscroll.data.entity.ChallengeHistory
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AppUsageRepository(
    private val appUsageDao: AppUsageDao,
    private val appSettingsDao: AppSettingsDao,
    private val challengeHistoryDao: ChallengeHistoryDao
) {
    // App Usage
    suspend fun getUsageForDate(date: Long, packageName: String): AppUsage? {
        return appUsageDao.getUsageForDate(date, packageName)
    }
    
    fun getUsageBetweenDates(startDate: Long, endDate: Long): Flow<List<AppUsage>> {
        return appUsageDao.getUsageBetweenDates(startDate, endDate)
    }
    
    suspend fun insertOrUpdateUsage(usage: AppUsage) {
        val existing = appUsageDao.getUsageForDate(usage.date, usage.packageName)
        if (existing != null) {
            appUsageDao.updateUsage(usage.copy(id = existing.id))
        } else {
            appUsageDao.insertUsage(usage)
        }
    }
    
    suspend fun getTotalUsageForDate(date: Long): Long {
        return appUsageDao.getTotalUsageForDate(date) ?: 0L
    }
    
    suspend fun getTotalBlocksForDate(date: Long): Int {
        return appUsageDao.getTotalBlocksForDate(date) ?: 0
    }
    
    // App Settings
    fun getAllSettings(): Flow<List<AppSettings>> {
        return appSettingsDao.getAllSettings()
    }
    
    fun getMonitoredApps(): Flow<List<AppSettings>> {
        return appSettingsDao.getMonitoredApps()
    }
    
    suspend fun getSettingsForPackage(packageName: String): AppSettings? {
        return appSettingsDao.getSettingsForPackage(packageName)
    }
    
    suspend fun insertOrUpdateSettings(settings: AppSettings) {
        appSettingsDao.insertSettings(settings)
    }
    
    suspend fun deleteSettings(settings: AppSettings) {
        appSettingsDao.deleteSettings(settings)
    }
    
    // Challenge History
    suspend fun getChallengesForDate(packageName: String, date: Long): List<ChallengeHistory> {
        return challengeHistoryDao.getChallengesForDate(packageName, date)
    }
    
    suspend fun getChallengeCountForDate(packageName: String, date: Long): Int {
        return challengeHistoryDao.getChallengeCountForDate(packageName, date)
    }
    
    suspend fun insertChallenge(challenge: ChallengeHistory) {
        challengeHistoryDao.insertChallenge(challenge)
    }
    
    fun getRecentChallenges(limit: Int = 50): Flow<List<ChallengeHistory>> {
        return challengeHistoryDao.getRecentChallenges(limit)
    }
    
    // Helper methods
    fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

