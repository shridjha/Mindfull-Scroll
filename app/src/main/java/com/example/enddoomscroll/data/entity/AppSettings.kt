package com.example.enddoomscroll.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isMonitored: Boolean = true,
    val timeLimitMinutes: Int = 30,
    val blockReelsShorts: Boolean = true,
    val enableMathChallenge: Boolean = true,
    val mathChallengeDifficulty: String = "Medium", // Easy, Medium, Hard
    val challengeTimeRewardMinutes: Int = 10,
    val maxChallengesPerDay: Int = 3
)
