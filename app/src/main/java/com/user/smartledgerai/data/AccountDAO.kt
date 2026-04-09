package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDAO {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllAccounts(): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteAccount(id: Int)

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getAccountById(id: Int): Flow<Account>

    @Query("""
        SELECT * FROM transactions 
        WHERE merchant LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
        ORDER BY timestamp DESC
    """)
    fun searchAccounts(query: String): Flow<List<Account>>

    @Query("""
        SELECT * FROM transactions 
        WHERE timestamp BETWEEN :startDate AND :endDate 
        ORDER BY timestamp DESC
    """)
    fun getAccountsByDateRange(startDate: Long, endDate: Long): Flow<List<Account>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE timestamp BETWEEN :startDate AND :endDate
    """)
    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double?>
}