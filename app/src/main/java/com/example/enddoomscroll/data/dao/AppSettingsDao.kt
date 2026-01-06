package com.example.enddoomscroll.data.dao

import androidx.room.*
import com.example.enddoomscroll.data.entity.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettings>>
    
    @Query("SELECT * FROM app_settings WHERE isMonitored = 1")
    fun getMonitoredApps(): Flow<List<AppSettings>>
    
    @Query("SELECT * FROM app_settings WHERE packageName = :packageName")
    suspend fun getSettingsForPackage(packageName: String): AppSettings?
    
    @Query("SELECT * FROM app_settings WHERE packageName = :packageName")
    fun getSettingsForPackageFlow(packageName: String): Flow<AppSettings?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
    
    @Update
    suspend fun updateSettings(settings: AppSettings)
    
    @Delete
    suspend fun deleteSettings(settings: AppSettings)
}
