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
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class TransactionViewModel @Inject constructor(private val transactionRepository: TransactionRepository): ViewModel() {
    val transactions: StateFlow<List<Transaction>> = transactionRepository.getAllAccounts.map{
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


}