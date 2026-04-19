package com.user.smartledgerai.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Transaction::class, Category::class, Account::class, Setting::class, AllowedApp::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(ClassConverter::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDAO

    abstract fun categoryDao(): CategoryDAO
    abstract fun settingDao(): SettingDAO
    abstract fun allowedAppDao(): AllowedAppDAO
}