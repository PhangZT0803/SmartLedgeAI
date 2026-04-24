package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDAO {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransaction(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE transactionId = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("""
        SELECT * FROM transactions 
        WHERE merchant LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
        ORDER BY timestamp DESC
    """)
    fun searchTransaction(query: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE timestamp BETWEEN :startDate AND :endDate 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE timestamp BETWEEN :startDate AND :endDate
    """)
    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double?>
}