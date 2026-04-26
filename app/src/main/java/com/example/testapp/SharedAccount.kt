package com.example.testapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shared_account_table")
data class SharedAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdBy: Int, // User ID who created the shared account
    val type: String, // "family", "couple", "roommates"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
