package com.user.smartledgerai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")

data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val transactionId: Int = 0,
    val timestamp: Long,
    val currency: String,
    val amount: Double,
    val transactionType: TransactionType, // income, spending, transfer
    val merchant: String, //To Do Account to
    val categoryId: Int,
    val description: String? = null,
    val source: String, //To Do Account from
    val rawData: String? = null, // Original notification/email content for verification
    var isVerified: Boolean = false,
)