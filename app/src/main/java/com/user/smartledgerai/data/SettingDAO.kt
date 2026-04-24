package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface SettingDAO {

    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Query("SELECT value FROM settings WHERE value = :key")
    fun getSettingValue(key: String): Flow<String?>


}