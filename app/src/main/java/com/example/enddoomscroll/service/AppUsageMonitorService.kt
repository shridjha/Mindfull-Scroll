//package com.example.enddoomscroll.service
//
//import android.app.*
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.os.Build
//import android.os.IBinder
//import androidx.core.app.NotificationCompat
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import com.example.enddoomscroll.R
//import com.example.enddoomscroll.data.database.AppDatabase
//import com.example.enddoomscroll.data.entity.AppUsage
//import com.example.enddoomscroll.data.repository.AppUsageRepository
//import com.example.enddoomscroll.MainActivity
//import com.example.enddoomscroll.ui.blocking.BlockingOverlayActivity
//import com.example.enddoomscroll.util.UsageStatsHelper
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//
//class AppUsageMonitorService : Service() {
//
//    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//    private lateinit var database: AppDatabase
//    private lateinit var repository: AppUsageRepository
//    private lateinit var usageStatsHelper: UsageStatsHelper
//
//    private val notificationId = 1
//    private val channelId = "app_usage_monitor_channel"
//
//    private var currentPackageName: String? = null
//    private var currentAppStartTime: Long = 0
//    private var lastUpdateTime: Long = 0
//
//    private val reelsBlockedReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == MindfulScrollAccessibilityService.ACTION_REELS_BLOCKED) {
//                val packageName = intent.getStringExtra(MindfulScrollAccessibilityService.EXTRA_PACKAGE_NAME)
//                packageName?.let { incrementBlocksForPackage(it) }
//            }
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        database = AppDatabase.getDatabase(this)
//        repository = AppUsageRepository(
//            database.appUsageDao(),
//            database.appSettingsDao(),
//            database.challengeHistoryDao()
//        )
//        usageStatsHelper = UsageStatsHelper(this)
//
//        createNotificationChannel()
//        startForeground(notificationId, createNotification())
//
//        // Register receiver for reels blocked events
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//            reelsBlockedReceiver,
//            IntentFilter(MindfulScrollAccessibilityService.ACTION_REELS_BLOCKED)
//        )
//
//        startMonitoring()
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return START_STICKY // Restart service if killed
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(reelsBlockedReceiver)
//        serviceScope.cancel()
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "App Usage Monitor",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Monitors app usage and enforces time limits"
//                setShowBadge(false)
//            }
//            val notificationManager = getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    private fun createNotification(): Notification {
//        val intent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        return NotificationCompat.Builder(this, channelId)
//            .setContentTitle("MindfulScroll is monitoring your usage")
//            .setContentText("Tracking app usage and blocking Reels/Shorts")
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentIntent(pendingIntent)
//            .setOngoing(true)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()
//    }
//
//    private fun startMonitoring() {
//        serviceScope.launch {
//            while (true) {
//                try {
//                    checkAndUpdateUsage()
//                    delay(5000) // Check every 5 seconds
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    delay(10000) // Wait longer on error
//                }
//            }
//        }
//    }
//
//    private suspend fun checkAndUpdateUsage() {
//        val monitoredApps = repository.getMonitoredApps().first()
//        val todayStart = repository.getTodayStartTimestamp()
//        val now = System.currentTimeMillis()
//
//        // Get current foreground app
//        val foregroundPackage = usageStatsHelper.getForegroundAppPackageName()
//
//        if (foregroundPackage != null && monitoredApps.any { it.packageName == foregroundPackage && it.isMonitored }) {
//            val settings = repository.getSettingsForPackage(foregroundPackage)
//            if (settings != null) {
//                // Get current usage for today
//                val usageToday = repository.getUsageForDate(todayStart, foregroundPackage)
//                    ?: AppUsage(
//                        packageName = foregroundPackage,
//                        date = todayStart,
//                        timeUsed = 0,
//                        timeLimit = settings.timeLimitMinutes * 60 * 1000L
//                    )
//
//                // Calculate new usage time
//                val currentUsage = usageStatsHelper.getUsageForToday(foregroundPackage)
//                val updatedUsage = usageToday.copy(
//                    timeUsed = currentUsage,
//                    blocksTriggered = usageToday.blocksTriggered
//                )
//
//                repository.insertOrUpdateUsage(updatedUsage)
//
//                // Check if time limit exceeded
//                val totalAllowedTime = updatedUsage.timeLimit + updatedUsage.extraTimeEarned
//                if (updatedUsage.timeUsed >= totalAllowedTime) {
//                    // Time limit exceeded - block the app
//                    blockApp(foregroundPackage, settings)
//                }
//            }
//        }
//    }
//
//    private fun incrementBlocksForPackage(packageName: String) {
//        serviceScope.launch {
//            val todayStart = repository.getTodayStartTimestamp()
//            val usage = repository.getUsageForDate(todayStart, packageName)
//            if (usage != null) {
//                val updated = usage.copy(blocksTriggered = usage.blocksTriggered + 1)
//                repository.insertOrUpdateUsage(updated)
//            }
//        }
//    }
//
//    private suspend fun blockApp(packageName: String, settings: com.example.enddoomscroll.data.entity.AppSettings) {
//        // Launch blocking overlay activity
//        val intent = Intent(this, BlockingOverlayActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            putExtra("package_name", packageName)
//            putExtra("time_limit", settings.timeLimitMinutes)
//        }
//        startActivity(intent)
//    }
//
//    companion object {
//        fun start(context: Context) {
//            val intent = Intent(context, AppUsageMonitorService::class.java)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent)
//            } else {
//                context.startService(intent)
//            }
//        }
//    }
//}


package com.example.enddoomscroll.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.enddoomscroll.MainActivity
import com.example.enddoomscroll.R
import com.example.enddoomscroll.data.database.AppDatabase
import com.example.enddoomscroll.data.entity.AppUsage
import com.example.enddoomscroll.data.entity.AppSettings
import com.example.enddoomscroll.data.repository.AppUsageRepository
import com.example.enddoomscroll.ui.blocking.BlockingOverlayActivity
import com.example.enddoomscroll.util.UsageStatsHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class AppUsageMonitorService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var database: AppDatabase
    private lateinit var repository: AppUsageRepository
    private lateinit var usageStatsHelper: UsageStatsHelper

    private val notificationId = 1
    private val channelId = "app_usage_monitor_channel"

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)
        repository = AppUsageRepository(
            database.appUsageDao(),
            database.appSettingsDao(),
            database.challengeHistoryDao()
        )
        usageStatsHelper = UsageStatsHelper(this)

        createNotificationChannel()
        startForeground(notificationId, createNotification())
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    // ---------------- NOTIFICATION ----------------

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "App Usage Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks app usage and enforces limits"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("MindfulScroll running")
            .setContentText("Monitoring app usage")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    // ---------------- CORE LOOP ----------------

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                try {
                    checkAndUpdateUsage()
                    delay(5000)
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(10000)
                }
            }
        }
    }

    private suspend fun checkAndUpdateUsage() {
        // ✅ FIX: collect Flow properly
        val monitoredApps: List<AppSettings> =
            repository.getMonitoredApps().first()

        if (monitoredApps.isEmpty()) return

        val foregroundPackage =
            usageStatsHelper.getForegroundAppPackageName() ?: return

        val settings = monitoredApps.firstOrNull {
            it.packageName == foregroundPackage && it.isMonitored
        } ?: return

        val todayStart = repository.getTodayStartTimestamp()
        val usedMillis = usageStatsHelper.getUsageForToday(foregroundPackage)

        val existingUsage = repository.getUsageForDate(todayStart, foregroundPackage)
            ?: AppUsage(
                packageName = foregroundPackage,
                date = todayStart,
                timeUsed = 0L,
                timeLimit = settings.timeLimitMinutes * 60_000L
            )

        val updatedUsage = existingUsage.copy(
            timeUsed = usedMillis
        )

        repository.insertOrUpdateUsage(updatedUsage)

        val allowedTime =
            updatedUsage.timeLimit + updatedUsage.extraTimeEarned

        if (updatedUsage.timeUsed >= allowedTime) {
            blockApp(foregroundPackage, settings)
        }
    }

    // ---------------- BLOCK ----------------

    private fun blockApp(packageName: String, settings: AppSettings) {
        val intent = Intent(this, BlockingOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("package_name", packageName)
            putExtra("time_limit", settings.timeLimitMinutes)
        }
        startActivity(intent)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppUsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
