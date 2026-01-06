package com.example.enddoomscroll.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "app_usage")
data class AppUsage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val date: Long, // Timestamp in milliseconds
    val timeUsed: Long, // Time in milliseconds
    val timeLimit: Long, // Time limit in milliseconds
    val blocksTriggered: Int = 0, // Number of times reels/shorts were blocked
    val challengesCompleted: Int = 0,
    val extraTimeEarned: Long = 0 // Extra time earned through challenges in milliseconds
)
