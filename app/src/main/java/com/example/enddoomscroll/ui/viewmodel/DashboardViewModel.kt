package com.example.enddoomscroll.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.enddoomscroll.data.database.AppDatabase
import com.example.enddoomscroll.data.repository.AppUsageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: AppUsageRepository
) : ViewModel() {
    
    val todayUsage = repository.getUsageBetweenDates(
        repository.getTodayStartTimestamp(),
        System.currentTimeMillis()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val monitoredApps = repository.getMonitoredApps().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    suspend fun getTotalUsageToday(): Long {
        return repository.getTotalUsageForDate(repository.getTodayStartTimestamp())
    }
    
    suspend fun getTotalBlocksToday(): Int {
        return repository.getTotalBlocksForDate(repository.getTodayStartTimestamp())
    }
    
    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = AppDatabase.getDatabase(context)
                    val repository = AppUsageRepository(
                        database.appUsageDao(),
                        database.appSettingsDao(),
                        database.challengeHistoryDao()
                    )
                    return DashboardViewModel(repository) as T
                }
            }
        }
    }
}

