package com.example.testapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transaction_table WHERE userId = :userId ORDER BY date DESC")
    fun getUserTransactions(userId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transaction_table WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getUserTransactionsByType(userId: Int, type: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transaction_table WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    fun getUserTransactionsByCategory(userId: Int, categoryId: Int): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transaction_table WHERE userId = :userId AND type = 'expense'")
    suspend fun getTotalExpenses(userId: Int): Double?

    @Query("SELECT SUM(amount) FROM transaction_table WHERE userId = :userId AND type = 'income'")
    suspend fun getTotalIncome(userId: Int): Double?

    @Query("SELECT SUM(amount) FROM transaction_table WHERE userId = :userId AND type = 'expense' AND date >= :startDate")
    suspend fun getMonthlyExpenses(userId: Int, startDate: Long): Double?

    @Query("SELECT categoryId, SUM(amount) as total FROM transaction_table WHERE userId = :userId AND type = 'expense' GROUP BY categoryId")
    suspend fun getExpensesByCategory(userId: Int): List<CategoryTotal>

    @Query("SELECT * FROM transaction_table WHERE sharedAccountId = :sharedAccountId ORDER BY date DESC")
    fun getSharedAccountTransactions(sharedAccountId: Int): Flow<List<Transaction>>
}

data class CategoryTotal(
    val categoryId: Int,
    val total: Double
)
