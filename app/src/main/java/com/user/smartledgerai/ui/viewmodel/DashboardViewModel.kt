package com.user.smartledgerai.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.user.smartledgerai.ai.AIParsingEngine
import com.user.smartledgerai.data.model.TransactionEntity
import com.user.smartledgerai.data.model.TransactionType
import com.user.smartledgerai.data.repository.TransactionRepository
import com.user.smartledgerai.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val parsingEngine: AIParsingEngine,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    // --- Form State ---
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

    fun clearSheetForm() {
        _sheetTitle.value = ""
        _sheetAmount.value = ""
        _sheetType.value = TransactionType.EXPENSE
        _sheetCategory.value = "Food"
    }

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Reactively filtered flow of transactions for the CURRENT MONTH.
     */
    private val currentMonthTransactions: Flow<List<TransactionEntity>> = transactions.map { list ->
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        list.filter { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }
    }

    val totalBalance: StateFlow<Double> = transactions.map { list ->
        list.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val monthlyIncome: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val monthlySpend: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val spendingByCategoryPercentages: StateFlow<List<Float>> = currentMonthTransactions.map { list ->
        val expenses = list.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenses.sumOf { it.amount }
        if (totalExpense == 0.0) return@map emptyList<Float>()

        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }

        categoryTotals.values.sortedDescending().take(3).map { (it / totalExpense).toFloat() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveNewTransaction(): Boolean {
        val rawTitle = _sheetTitle.value.trim()
        val amountStr = _sheetAmount.value.trim()
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val category = _sheetCategory.value
        val type = _sheetType.value
        val finalTitle = if (rawTitle.isBlank()) category else rawTitle

        if (amount > 0) {
            val newTx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                title = finalTitle,
                amount = amount,
                category = category,
                time = "Just now",
                timestamp = System.currentTimeMillis(),
                type = type,
                icon = when (category) {
                    "Food" -> Icons.Default.Fastfood
                    "Drinks" -> Icons.Default.LocalCafe
                    "Transport" -> Icons.Default.DirectionsCar
                    "Shopping" -> Icons.Default.ShoppingBag
                    "Salary" -> Icons.Default.AccountBalance
                    else -> Icons.Default.ReceiptLong
                }
            )
            repository.addTransaction(newTx)
            clearSheetForm()
            return true
        }
        return false
    }

    fun simulateIngestion(rawText: String) {
        viewModelScope.launch {
            val parsedResult = parsingEngine.parseRawData(rawText)
            repository.addPendingTransaction(parsedResult)
        }
    }

    fun syncAI() {
        val mockRawStrings = listOf(
            "GRAB* 123456 - MYR 24.50 - FOOD",
            "MYEG RM 92.50 PETROL",
            "STARBUCKS KUALA LUMPUR RM 18.00",
            "UNQLO MIDVALLEY MYR 199.90"
        )
        simulateIngestion(mockRawStrings.random())
    }

    // --- Budget Prediction Logic (Strictly Reactive) ---
    val budgetPredictionText: StateFlow<String> = combine(currentMonthTransactions, userPreferences.monthlyBudget) { list, budget ->
        val currentSpend = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val proRatedBudget = (currentDay.toDouble() / maxDays.toDouble()) * budget
        
        if (proRatedBudget >= currentSpend) {
            val diff = proRatedBudget - currentSpend
            "You're RM ${String.format(Locale.US, "%.0f", diff)} ahead of your savings goal this month!"
        } else {
            val diff = currentSpend - proRatedBudget
            "Careful! You're RM ${String.format(Locale.US, "%.0f", diff)} over your planned budget today."
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Calculating...")

    val isPredictionPositive: StateFlow<Boolean> = combine(currentMonthTransactions, userPreferences.monthlyBudget) { list, budget ->
        val currentSpend = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val proRatedBudget = (currentDay.toDouble() / maxDays.toDouble()) * budget
        proRatedBudget >= currentSpend
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}
