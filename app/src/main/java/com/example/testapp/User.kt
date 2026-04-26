package com.example.testapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val email: String = "",
    val phone: String = "",
    val language: String = "en",
    val darkMode: Boolean = false,
    val biometricEnabled: Boolean = false,
    val notificationEmail: Boolean = true,
    val notificationSMS: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
