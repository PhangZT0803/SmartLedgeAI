package com.user.smartledgerai.di

import android.content.Context
import androidx.room.Room
import com.user.smartledgerai.data.TransactionDatabase
import com.user.smartledgerai.data.TransactionRepository
import com.user.smartledgerai.data.AllowedAppDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TransactionDatabase =
        Room.databaseBuilder(
            context,
            TransactionDatabase::class.java,
            "smartledger.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideAccountRepository(db: TransactionDatabase): TransactionRepository =
        TransactionRepository(db.transactionDao(),db.categoryDao(), db.settingDao(),db.allowedAppDao())

    @Provides
    fun provideAllowedAppDao(db: TransactionDatabase): AllowedAppDAO =
        db.allowedAppDao()
}