package com.example.enddoomscroll.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.enddoomscroll.service.AppUsageMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the monitoring service after boot
            AppUsageMonitorService.start(context)
        }
    }
}

