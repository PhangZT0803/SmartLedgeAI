package com.user.smartledgerai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.user.smartledgerai.data.model.TransactionEntity
import com.user.smartledgerai.data.model.TransactionType
import com.user.smartledgerai.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _sortLatestFirst = MutableStateFlow(true)
    val sortLatestFirst: StateFlow<Boolean> = _sortLatestFirst

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        repository.getTransactions(),
        _selectedFilter,
        _sortLatestFirst
    ) { transactions, filter, sortLatest ->
        val filtered = when (filter) {
            "Income" -> transactions.filter { it.type == TransactionType.INCOME }
            "Expense" -> transactions.filter { it.type == TransactionType.EXPENSE }
            "Food" -> transactions.filter { it.category == "Food" }
            "Drinks" -> transactions.filter { it.category == "Drinks" }
            "Transport" -> transactions.filter { it.category == "Transport" }
            "Shopping" -> transactions.filter { it.category == "Shopping" }
            else -> transactions
        }
        if (sortLatest) filtered else filtered.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun toggleSort() {
        _sortLatestFirst.value = !_sortLatestFirst.value
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        repository.deleteTransaction(transaction.id)
    }
}
