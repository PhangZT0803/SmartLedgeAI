package com.user.smartledgerai.di

import com.user.smartledgerai.data.repository.RoomTransactionRepository
import com.user.smartledgerai.data.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        roomTransactionRepository: RoomTransactionRepository
    ): TransactionRepository
}
