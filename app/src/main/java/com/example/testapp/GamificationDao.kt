package com.example.testapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface GamificationDao {
    // Milestone operations
    @Insert
    suspend fun insertMilestone(milestone: Milestone)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Query("SELECT * FROM milestone_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserMilestones(userId: Int): Flow<List<Milestone>>

    @Query("SELECT * FROM milestone_table WHERE userId = :userId AND isCompleted = 0")
    suspend fun getActiveMilestones(userId: Int): List<Milestone>

    @Query("SELECT * FROM milestone_table WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedMilestones(userId: Int): Flow<List<Milestone>>

    // Reward operations
    @Insert
    suspend fun insertReward(reward: Reward)

    @Query("SELECT * FROM reward_table WHERE userId = :userId AND isRedeemed = 0")
    suspend fun getAvailableRewards(userId: Int): List<Reward>

    @Query("SELECT * FROM reward_table WHERE userId = :userId ORDER BY pointsRequired ASC")
    fun getAllRewards(userId: Int): Flow<List<Reward>>

    // User points operations
    @Query("SELECT * FROM user_points_table WHERE userId = :userId")
    suspend fun getUserPoints(userId: Int): UserPoints?

    @Insert
    suspend fun insertUserPoints(userPoints: UserPoints)

    @Update
    suspend fun updateUserPoints(userPoints: UserPoints)

    @Query("UPDATE user_points_table SET totalPoints = totalPoints + :points, lastUpdated = :timestamp WHERE userId = :userId")
    suspend fun addPoints(userId: Int, points: Int, timestamp: Long)

    @Query("UPDATE user_points_table SET currentStreak = :streak, longestStreak = CASE WHEN :streak > longestStreak THEN :streak ELSE longestStreak END WHERE userId = :userId")
    suspend fun updateStreak(userId: Int, streak: Int)
}
