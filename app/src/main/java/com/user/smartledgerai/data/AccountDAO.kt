package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDAO {
    @Query("SELECT * FROM Account")
    fun getAllAccounts(): Flow<List<Account>>
}