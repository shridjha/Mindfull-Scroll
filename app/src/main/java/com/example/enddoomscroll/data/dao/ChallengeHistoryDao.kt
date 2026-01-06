package com.example.enddoomscroll.data.dao

import androidx.room.*
import com.example.enddoomscroll.data.entity.ChallengeHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeHistoryDao {
    @Query("SELECT * FROM challenge_history WHERE date = :date AND packageName = :packageName")
    suspend fun getChallengesForDate(packageName: String, date: Long): List<ChallengeHistory>
    
    @Query("SELECT COUNT(*) FROM challenge_history WHERE date = :date AND packageName = :packageName")
    suspend fun getChallengeCountForDate(packageName: String, date: Long): Int
    
    @Query("SELECT * FROM challenge_history WHERE date = :date")
    suspend fun getAllChallengesForDate(date: Long): List<ChallengeHistory>
    
    @Insert
    suspend fun insertChallenge(challenge: ChallengeHistory)
    
    @Query("SELECT * FROM challenge_history ORDER BY date DESC LIMIT :limit")
    fun getRecentChallenges(limit: Int): Flow<List<ChallengeHistory>>
}
