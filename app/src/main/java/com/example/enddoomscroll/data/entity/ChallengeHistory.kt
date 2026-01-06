package com.example.enddoomscroll.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_history")
data class ChallengeHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val date: Long, // Timestamp in milliseconds
    val difficulty: String,
    val wasSuccessful: Boolean,
    val timeRewarded: Long, // Time rewarded in milliseconds
    val challengeNumber: Int // Which challenge of the day (1, 2, 3, etc.)
)
