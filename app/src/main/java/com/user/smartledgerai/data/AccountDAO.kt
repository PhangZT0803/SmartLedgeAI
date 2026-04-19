package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDAO {
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>
}