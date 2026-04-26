package com.example.testapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM category_table ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category_table WHERE isDefault = 1")
    suspend fun getDefaultCategories(): List<Category>

    @Query("SELECT * FROM category_table WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): Category?

    @Query("UPDATE category_table SET budgetLimit = :limit WHERE id = :categoryId")
    suspend fun updateCategoryBudgetLimit(categoryId: Int, limit: Double)
}
