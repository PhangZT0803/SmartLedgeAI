package com.user.smartledgerai.di

import android.content.Context
import androidx.room.Room
import com.user.smartledgerai.data.AccountDatabase
import com.user.smartledgerai.data.AccountRepository
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
    fun provideDatabase(@ApplicationContext context: Context): AccountDatabase =
        Room.databaseBuilder(
            context,
            AccountDatabase::class.java,
            "smartledger.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideAccountRepository(db: AccountDatabase): AccountRepository =
        AccountRepository(db.accountDao(), db.settingDao())
}