package com.user.smartledgerai.data.repository

import androidx.room.*
import com.user.smartledgerai.data.model.PendingTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTransactionDao {
    @Query("SELECT * FROM pending_transactions ORDER BY rowid ASC")
    fun getAllPendingTransactions(): Flow<List<PendingTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingTransaction(transaction: PendingTransactionEntity)

    @Query("DELETE FROM pending_transactions WHERE id = :id")
    suspend fun deletePendingTransaction(id: String)
    
    @Delete
    suspend fun delete(transaction: PendingTransactionEntity)
}
