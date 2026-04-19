package com.user.smartledgerai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="Category" )
data class Category (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name:String,
    val type: TransactionType,
    val iconName: String? = null
    )