package com.example.testapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedAccountDao {
    @Insert
    suspend fun insertSharedAccount(sharedAccount: SharedAccount): Long

    @Update
    suspend fun updateSharedAccount(sharedAccount: SharedAccount)

    @Delete
    suspend fun deleteSharedAccount(sharedAccount: SharedAccount)

    @Query("SELECT * FROM shared_account_table WHERE createdBy = :userId")
    fun getUserSharedAccounts(userId: Int): Flow<List<SharedAccount>>

    @Query("SELECT * FROM shared_account_table WHERE id = :accountId")
    suspend fun getSharedAccountById(accountId: Int): SharedAccount?

    @Insert
    suspend fun addMember(member: SharedAccountMember)

    @Delete
    suspend fun removeMember(member: SharedAccountMember)

    @Query("SELECT * FROM shared_account_member_table WHERE sharedAccountId = :accountId")
    fun getAccountMembers(accountId: Int): Flow<List<SharedAccountMember>>

    @Query("SELECT * FROM shared_account_member_table WHERE userId = :userId")
    fun getUserMemberships(userId: Int): Flow<List<SharedAccountMember>>

    @Query("UPDATE shared_account_member_table SET canApproveTransactions = :canApprove WHERE sharedAccountId = :accountId AND userId = :userId")
    suspend fun updateApprovalPermission(accountId: Int, userId: Int, canApprove: Boolean)
}
