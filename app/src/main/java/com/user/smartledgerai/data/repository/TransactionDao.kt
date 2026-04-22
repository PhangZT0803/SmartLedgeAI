package com.user.smartledgerai.data.repository

import androidx.room.*
import com.user.smartledgerai.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY rowid DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("SELECT * FROM transactions WHERE title LIKE '%' || :query || '%' OR category = :query ORDER BY rowid DESC LIMIT 2")
    fun getRecentSimilarTransactions(query: String): Flow<List<TransactionEntity>>
}
