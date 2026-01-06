package com.example.enddoomscroll.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import java.util.concurrent.TimeUnit

class UsageStatsHelper(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    
    fun getUsageStatsForPackage(packageName: String, startTime: Long, endTime: Long): UsageStats? {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        return stats.find { it.packageName == packageName }
    }
    
    fun getCurrentUsageForPackage(packageName: String): Long {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(24)
        val stats = getUsageStatsForPackage(packageName, startTime, endTime)
        return stats?.totalTimeInForeground ?: 0L
    }
    
    fun getUsageForToday(packageName: String): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        val stats = getUsageStatsForPackage(packageName, startTime, endTime)
        return stats?.totalTimeInForeground ?: 0L
    }
    
    fun getForegroundAppPackageName(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.MINUTES.toMillis(1)
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            )
            
            if (stats.isNotEmpty()) {
                stats.sortByDescending { it.lastTimeUsed }
                return stats[0].packageName
            }
        }
        return null
    }
    
    companion object {
        fun isUsageStatsPermissionGranted(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
            val mode = appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            return mode == android.app.AppOpsManager.MODE_ALLOWED
        }
    }
}

