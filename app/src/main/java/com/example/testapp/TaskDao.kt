package com.example.testapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserTasks(userId: Int): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getPendingTasks(userId: Int): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE userId = :userId AND isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedTasks(userId: Int): Flow<List<Task>>

    @Query("DELETE FROM task_table WHERE userId = :userId")
    suspend fun deleteAllUserTasks(userId: Int)
}
