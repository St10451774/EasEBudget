package com.example.testapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "shared_account_member_table",
    foreignKeys = [
        ForeignKey(
            entity = SharedAccount::class,
            parentColumns = ["id"],
            childColumns = ["sharedAccountId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class SharedAccountMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sharedAccountId: Int,
    val userId: Int,
    val role: String, // "owner", "admin", "member"
    val canAddTransactions: Boolean = true,
    val canApproveTransactions: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)
