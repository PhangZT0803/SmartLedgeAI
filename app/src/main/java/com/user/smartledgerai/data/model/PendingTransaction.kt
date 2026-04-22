package com.user.smartledgerai.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_transactions")
data class PendingTransactionEntity(
    @PrimaryKey
    val id: String,
    val rawSource: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: TransactionType,
    val iconName: String,
    val matchPercentage: Int
) {
    val icon: ImageVector
        get() = IconMapper.getIcon(iconName)
}
