package com.example.testapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun registerUser(user: User)

    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM user_table WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM user_table WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE user_table SET biometricEnabled = :enabled WHERE id = :userId")
    suspend fun updateBiometricSetting(userId: Int, enabled: Boolean)

    @Query("UPDATE user_table SET darkMode = :darkMode WHERE id = :userId")
    suspend fun updateDarkModeSetting(userId: Int, darkMode: Boolean)

    @Query("UPDATE user_table SET language = :language WHERE id = :userId")
    suspend fun updateLanguageSetting(userId: Int, language: String)

    @Query("UPDATE user_table SET notificationEmail = :enabled, notificationSMS = :smsEnabled WHERE id = :userId")
    suspend fun updateNotificationSettings(userId: Int, enabled: Boolean, smsEnabled: Boolean)

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<User>
}
