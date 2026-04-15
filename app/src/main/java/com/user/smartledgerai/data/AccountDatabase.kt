package com.user.smartledgerai.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Account::class, Setting::class, AllowedApp::class],
    version = 1,
    exportSchema = false
)

abstract class AccountDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDAO
    abstract fun settingDao(): SettingDAO

    abstract fun allowedAppDao(): AllowedAppDAO
}