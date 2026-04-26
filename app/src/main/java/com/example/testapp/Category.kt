package com.example.testapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: String = "#FF6200EE", // Default color
    val icon: String = "default", // Icon name or resource
    val budgetLimit: Double = 0.0, // Monthly budget limit for this category
    val isDefault: Boolean = false // Default categories like "Food", "Transport", etc.
)
