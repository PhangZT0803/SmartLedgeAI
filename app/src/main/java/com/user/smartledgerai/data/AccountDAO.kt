package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDAO {
    @Query("SELECT * FROM Account")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM Account")
    suspend fun getAccountList(): List<Account>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Query("DELETE FROM Account WHERE accountId = :id")
    suspend fun delete(id: Int)
    @Query("SELECT * FROM Account WHERE packageName = :packageName LIMIT 1")
    suspend fun getAccountByPackage(packageName: String): Account?
}