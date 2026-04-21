package com.user.smartledgerai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.user.smartledgerai.data.Category
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionRepository
import com.user.smartledgerai.data.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun getAllCategories(): StateFlow<List<Category>> = transactionRepository.getAllCategories().map{
        categoryList -> categoryList.sortedBy {
            category -> category.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCategoriesByType(transactionType: TransactionType): StateFlow<List<Category>> = transactionRepository.getCategoriesByType(transactionType).map{
        categoryList -> categoryList.sortedBy {
            category -> category.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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