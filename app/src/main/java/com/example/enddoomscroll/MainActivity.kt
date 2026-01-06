package com.example.enddoomscroll

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.enddoomscroll.service.AppUsageMonitorService
import com.example.enddoomscroll.ui.dashboard.DashboardScreen
import com.example.enddoomscroll.ui.settings.AppPickerScreen
import com.example.enddoomscroll.ui.settings.SettingsScreen
import com.example.enddoomscroll.ui.theme.EndDoomScrollTheme

class MainActivity : ComponentActivity() {
    
    enum class Screen {
        Dashboard, Settings, AppPicker
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start the monitoring service
        AppUsageMonitorService.start(this)
        
        setContent {
            var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
            
            EndDoomScrollTheme {
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(
                        onNavigateToSettings = {
                            currentScreen = Screen.Settings
                        }
                    )
                    Screen.Settings -> SettingsScreen(
                        onNavigateBack = {
                            currentScreen = Screen.Dashboard
                        },
                        onOpenUsageStatsSettings = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            startActivity(intent)
                        },
                        onOpenAccessibilitySettings = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            startActivity(intent)
                        },
                        onOpenOverlaySettings = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                    data = Uri.parse("package:$packageName")
                                }
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:$packageName")
                                }
                            }
                            startActivity(intent)
                        },
                        onNavigateToAppPicker = {
                            currentScreen = Screen.AppPicker
                        }
                    )
                    Screen.AppPicker -> AppPickerScreen(
                        onNavigateBack = {
                            currentScreen = Screen.Settings
                        }
                    )
                }
            }
        }
    }
}