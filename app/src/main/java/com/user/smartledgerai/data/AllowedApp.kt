package com.user.smartledgerai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="AllowedApp")
data class AllowedApp(
    @PrimaryKey val packageName: String,
    val appName: String
)
