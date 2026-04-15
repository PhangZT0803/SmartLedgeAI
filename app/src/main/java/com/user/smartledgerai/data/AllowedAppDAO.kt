package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AllowedAppDAO {
    @Query("SELECT * FROM AllowedApp")
    fun  getAllAllowedApp(): Flow<List<AllowedApp>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(app: AllowedApp)

    @Query("DELETE FROM AllowedApp WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}