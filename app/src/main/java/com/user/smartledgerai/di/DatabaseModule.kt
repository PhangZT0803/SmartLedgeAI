package com.user.smartledgerai.di

import android.content.Context
import androidx.room.Room
import com.user.smartledgerai.data.repository.AppDatabase
import com.user.smartledgerai.data.repository.TransactionDao
import com.user.smartledgerai.data.repository.PendingTransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smartledger_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    @Singleton
    fun providePendingTransactionDao(database: AppDatabase): PendingTransactionDao {
        return database.pendingTransactionDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): com.user.smartledgerai.data.repository.UserPreferencesRepository {
        return com.user.smartledgerai.data.repository.UserPreferencesRepository(context)
    }
}
