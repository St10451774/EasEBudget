package com.example.testapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestone_table")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val type: String, // "budget_streak", "savings_goal", "category_limit"
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val isCompleted: Boolean = false,
    val rewardPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "reward_table")
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val pointsRequired: Int,
    val isRedeemed: Boolean = false,
    val redeemedAt: Long? = null
)

@Entity(tableName = "user_points_table")
data class UserPoints(
    @PrimaryKey val userId: Int,
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
