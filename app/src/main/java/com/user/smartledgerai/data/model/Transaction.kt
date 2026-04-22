package com.user.smartledgerai.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val time: String, // String for simplicity in mock, as per original code
    val timestamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
    val icon: ImageVector
)
