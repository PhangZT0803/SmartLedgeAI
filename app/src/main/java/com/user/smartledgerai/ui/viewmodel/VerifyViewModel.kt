package com.user.smartledgerai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.user.smartledgerai.data.model.PendingTransactionEntity
import com.user.smartledgerai.data.model.TransactionEntity
import com.user.smartledgerai.data.model.TransactionType
import com.user.smartledgerai.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    /**
     * Observe the database flow directly.
     */
    val pendingTransactions: StateFlow<List<PendingTransactionEntity>> = repository.getPendingTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Always pick the first item from the current list.
     */
    val currentPendingTransaction: StateFlow<PendingTransactionEntity?> = pendingTransactions
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Fetches recent similar past transactions based on the current pending item.
     * Uses the merchant title or category to search the main database.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val similarTransactions: StateFlow<List<TransactionEntity>> = currentPendingTransaction
        .flatMapLatest { pending ->
            if (pending != null) {
                // Try searching by title first, then category
                repository.getSimilarTransactions(pending.title)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Form State for manual edit bottom sheet ---
    private val _sheetTitle = MutableStateFlow("")
    val sheetTitle = _sheetTitle.asStateFlow()

    private val _sheetAmount = MutableStateFlow("")
    val sheetAmount = _sheetAmount.asStateFlow()

    private val _sheetType = MutableStateFlow(TransactionType.EXPENSE)
    val sheetType = _sheetType.asStateFlow()

    private val _sheetCategory = MutableStateFlow("Food")
    val sheetCategory = _sheetCategory.asStateFlow()

    fun updateSheetTitle(title: String) { _sheetTitle.value = title }
    fun updateSheetAmount(amount: String) {
        val normalized = amount.replace(',', '.')
        if (normalized.isEmpty() || normalized.all { it.isDigit() || it == '.' }) {
            if (normalized.count { it == '.' } <= 1) {
                _sheetAmount.value = normalized
            }
        }
    }
    fun updateSheetType(type: TransactionType) { _sheetType.value = type }
    fun updateSheetCategory(category: String) { _sheetCategory.value = category }

    fun prefillSheet() {
        val current = currentPendingTransaction.value ?: return
        _sheetTitle.value = current.title
        _sheetAmount.value = String.format(java.util.Locale.US, "%.2f", current.amount)
        _sheetType.value = current.type
        _sheetCategory.value = current.category
    }

    // Confirm as-is
    fun confirmAndSave(onSaved: () -> Unit) {
        val current = currentPendingTransaction.value ?: return
        repository.verifyAndCommit(current)
        onSaved()
    }

    // Save edited values from BottomSheet
    fun saveManualEdit(onSaved: () -> Unit): Boolean {
        val amountValue = _sheetAmount.value.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) return false
        
        val current = currentPendingTransaction.value ?: return false

        val updatedPending = current.copy(
            title = if (_sheetTitle.value.isEmpty()) _sheetCategory.value else _sheetTitle.value,
            amount = amountValue,
            category = _sheetCategory.value,
            type = _sheetType.value
        )
        
        repository.verifyAndCommit(updatedPending)
        onSaved()
        return true
    }
}
