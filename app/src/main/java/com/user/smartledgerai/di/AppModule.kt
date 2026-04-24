package com.user.smartledgerai.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.user.smartledgerai.data.TransactionDatabase
import com.user.smartledgerai.data.TransactionRepository
import com.user.smartledgerai.data.AllowedAppDAO
import com.user.smartledgerai.data.TransactionType
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
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val spending = TransactionType.SPENDING.name
                    val income = TransactionType.INCOME.name

                    val categories = listOf(
                        "('Food & Dining', '$spending')",
                        "('Transport', '$spending')",
                        "('Shopping', '$spending')",
                        "('Bills & Utilities', '$spending')",
                        "('Entertainment', '$spending')",
                        "('Health', '$spending')",
                        "('Education', '$spending')",
                        "('Groceries', '$spending')",
                        "('Others', '$spending')",
                        "('Salary', '$income')",
                        "('Freelance', '$income')",
                        "('Investment', '$income')",
                        "('Gift', '$income')",
                        "('Others', '$income')"
                    )

                    categories.forEach { values ->
                        db.execSQL("INSERT INTO Category (name, type) VALUES $values")
                    }
                }
            })
            .build()


    @Provides
    @Singleton
    fun provideTransactionRepository(db: TransactionDatabase): TransactionRepository =
        TransactionRepository(db.transactionDao(),db.categoryDao(), db.settingDao(),db.allowedAppDao(),db.accountDao())

    @Provides
    fun provideAllowedAppDao(db: TransactionDatabase): AllowedAppDAO =
        db.allowedAppDao()

    @Provides
    fun provideTransactionDao(db: TransactionDatabase) =
        db.transactionDao()

    @Provides
    fun provideAccountDao(db: TransactionDatabase) =
        db.accountDao()
}