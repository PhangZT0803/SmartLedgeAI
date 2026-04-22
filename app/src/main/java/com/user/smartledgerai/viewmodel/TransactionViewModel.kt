package com.user.smartledgerai.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.user.smartledgerai.data.Category
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionRepository
import com.user.smartledgerai.data.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class TransactionViewModel @Inject constructor(private val transactionRepository: TransactionRepository): ViewModel() {
    val transactions: StateFlow<List<Transaction>> = transactionRepository.getAllTransaction.map{
        transactionList -> transactionList.sortedBy {
            transaction -> transaction.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = transactionRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryMapping: StateFlow<Map<Int, String>> = allCategories.map { categories ->
        categories.associate { it.id to it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            transactionRepository.getAllCategories().collect { cats ->
                if (cats.isEmpty()) {
                    val defaults = listOf(
                        Category(name = "Food", type = TransactionType.SPENDING, iconName = "restaurant"),
                        Category(name = "Transport", type = TransactionType.SPENDING, iconName = "directions_car"),
                        Category(name = "Shopping", type = TransactionType.SPENDING, iconName = "shopping_bag"),
                        Category(name = "Drinks", type = TransactionType.SPENDING, iconName = "coffee"),
                        Category(name = "Housing", type = TransactionType.SPENDING, iconName = "home"),
                        Category(name = "Bills", type = TransactionType.SPENDING, iconName = "receipt"),
                        Category(name = "Salary", type = TransactionType.INCOME, iconName = "payments"),
                        Category(name = "Freelance", type = TransactionType.INCOME, iconName = "work")
                    )
                    defaults.forEach { transactionRepository.insertCategory(it) }
                }
                return@collect
            }
        }
    }
    fun getCategoriesByType(transactionType: TransactionType): Flow<List<Category>> {
        return transactionRepository.getCategoriesByType(transactionType)
    }

    fun insertTransaction(transaction: Transaction)= viewModelScope.launch {
        transactionRepository.insert(transaction)
    }

    fun deleteTransaction(transaction: Transaction)= viewModelScope.launch{
        transactionRepository.delete(transaction.transactionId)
    }

    fun verifyTransaction(original: Transaction,
                          amount: Double,
                          currency: String,
                          merchant: String,
                          source: String,
                          categoryId: Int,
                          timestamp: Long,
                          description: String?
    ) = viewModelScope.launch {
        transactionRepository.insert(
            original.copy(
                amount = amount,
                currency = currency,
                merchant = merchant,
                source = source,
                categoryId = categoryId,
                timestamp = timestamp,
                description = description,
                isVerified = true
            )
        )
    }
    fun insertCategory(category: Category) = viewModelScope.launch {
        transactionRepository.insertCategory(category)
    }
}