package com.user.smartledgerai.data.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.user.smartledgerai.data.model.Converters
import com.user.smartledgerai.data.model.PendingTransactionEntity
import com.user.smartledgerai.data.model.TransactionEntity

@Database(entities = [TransactionEntity::class, PendingTransactionEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun pendingTransactionDao(): PendingTransactionDao
}
