package com.example.testapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Transaction::class,
        Category::class,
        SharedAccount::class,
        SharedAccountMember::class,
        Milestone::class,
        Reward::class,
        UserPoints::class
    ], 
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun sharedAccountDao(): SharedAccountDao
    abstract fun gamificationDao(): GamificationDao
}
