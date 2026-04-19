package com.user.smartledgerai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Account")
data class Account (
    @PrimaryKey
    val accountId:Int,
    val accountName:String
)