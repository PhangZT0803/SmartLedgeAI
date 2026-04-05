package com.user.smartledgerai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val timestamp: Long,
    val amount: Double,
    val merchant: String,
    val category: String,
    val description: String? = null,
    val source: String,
    val rawData: String? = null,
    val isVerified: Boolean = false
)