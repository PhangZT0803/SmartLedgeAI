package com.user.smartledgerai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDAO {

        @Query("SELECT * FROM category ORDER BY name ASC") //Categories Screen
        fun getAllCategories(): Flow<List<Category>>


        @Query("SELECT * FROM category WHERE type = :transactionType ORDER BY name ASC")//Verify/NewTransaction Screen
        fun getCategoriesByType(transactionType: TransactionType): Flow<List<Category>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertCategory(category: Category)
}