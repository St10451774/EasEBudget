package com.example.testapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "shared_account_member_table",
    foreignKeys = [
        ForeignKey(
            entity = SharedAccount::class,
            parentColumns = ["id"],
            childColumns = ["sharedAccountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
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
