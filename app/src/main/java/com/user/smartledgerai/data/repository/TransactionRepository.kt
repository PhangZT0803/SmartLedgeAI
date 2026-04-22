package com.user.smartledgerai.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.user.smartledgerai.data.model.TransactionEntity
import com.user.smartledgerai.data.model.PendingTransactionEntity
import com.user.smartledgerai.data.model.TransactionType
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface TransactionRepository {
    fun getTransactions(): Flow<List<TransactionEntity>>
    fun addTransaction(transaction: TransactionEntity)
    fun deleteTransaction(id: String)
    
    fun getPendingTransactions(): Flow<List<PendingTransactionEntity>>
    fun addPendingTransaction(transaction: PendingTransactionEntity)
    fun deletePendingTransaction(id: String)
    
    // Transactional commit from pending to confirmed
    fun verifyAndCommit(pending: PendingTransactionEntity)

    fun getSimilarTransactions(query: String): Flow<List<TransactionEntity>>
}

@Singleton
class MockTransactionRepository @Inject constructor() : TransactionRepository {
    private val _transactions = MutableStateFlow(
        listOf(
            TransactionEntity(UUID.randomUUID().toString(), "Bubble Tea - CoCo", 18.90, "Drinks", "Today, 02:30 PM", System.currentTimeMillis(), TransactionType.EXPENSE, Icons.Default.LocalCafe),
            TransactionEntity(UUID.randomUUID().toString(), "Groceries - AEON", 150.00, "Food", "Today, 10:45 AM", System.currentTimeMillis(), TransactionType.EXPENSE, Icons.Default.LocalGroceryStore),
            TransactionEntity(UUID.randomUUID().toString(), "Salary Deposit", 4200.00, "Income", "Today, 09:15 AM", System.currentTimeMillis(), TransactionType.INCOME, Icons.Default.AccountBalance),
        )
    )

    override fun getTransactions(): Flow<List<TransactionEntity>> = _transactions.asStateFlow()

    override fun addTransaction(transaction: TransactionEntity) {
        _transactions.update { current -> listOf(transaction) + current }
    }

    override fun deleteTransaction(id: String) {
        _transactions.update { current -> current.filter { it.id != id } }
    }

    override fun getPendingTransactions(): Flow<List<PendingTransactionEntity>> = flowOf(emptyList())
    override fun addPendingTransaction(transaction: PendingTransactionEntity) {}
    override fun deletePendingTransaction(id: String) {}
    
    override fun verifyAndCommit(pending: PendingTransactionEntity) {
        val newTx = TransactionEntity(
            id = UUID.randomUUID().toString(),
            title = pending.title,
            amount = pending.amount,
            category = pending.category,
            time = "Just now",
            timestamp = pending.timestamp,
            type = pending.type,
            icon = pending.icon
        )
        addTransaction(newTx)
        deletePendingTransaction(pending.id)
    }

    override fun getSimilarTransactions(query: String): Flow<List<TransactionEntity>> {
        return _transactions.map { list: List<TransactionEntity> ->
            list.filter { it.title.contains(query, ignoreCase = true) || it.category.equals(query, ignoreCase = true) }
                .take(2)
        }
    }
}
