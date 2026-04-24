package com.user.smartledgerai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Account")
data class Account (
    @PrimaryKey(autoGenerate = true)
    val accountId:Int=0,
    val accountName:String,
    val packageName:String? = null
)