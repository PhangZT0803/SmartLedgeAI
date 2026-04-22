package com.user.smartledgerai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class InsightsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val monthlyBudget: StateFlow<Double> = userPreferences.monthlyBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000.0)

    fun updateBudget(newBudget: Double) {
        viewModelScope.launch {
            userPreferences.updateMonthlyBudget(newBudget)
        }
    }

    /**
     * Helper flow that filters transactions for the CURRENT MONTH and CURRENT YEAR.
     * This ensures strict reactivity and accuracy for monthly goal tracking.
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

    /**
     * Group transactions by Week of Month (W1, W2, W3, W4).
     */
    val weeklySpending: StateFlow<List<Double>> = currentMonthTransactions.map { list ->
        val expenses = list.filter { it.type == TransactionType.EXPENSE }
        val weekBuckets = mutableListOf(0.0, 0.0, 0.0, 0.0)
        expenses.forEach { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            val week = txCal.get(Calendar.WEEK_OF_MONTH)
            val index = (week - 1).coerceIn(0, 3)
            weekBuckets[index] += tx.amount
        }
        weekBuckets
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(0.0, 0.0, 0.0, 0.0))

    val maxWeeklySpend: StateFlow<Double> = weeklySpending.map { list ->
        list.maxOrNull() ?: 1.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0)

    /**
     * AI Analysis Logic (Truly Dynamic)
     */
    val analysisAlert: StateFlow<String?> = currentMonthTransactions.map { list ->
        val categorySpending = list.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
        
        val highestEntry = categorySpending.maxByOrNull { it.value }
        
        if (highestEntry != null && highestEntry.value > 0) {
            "Your ${highestEntry.key} expenses are high at RM ${String.format(Locale.US, "%.2f", highestEntry.value)}. Consider scaling back this week."
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val analysisPraise: StateFlow<String?> = currentMonthTransactions.map { list ->
        val categorySpending = list.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
        
        val lowestEntry = categorySpending.filter { it.value > 0 }.minByOrNull { it.value }
        
        if (lowestEntry != null) {
            "Great job! You kept ${lowestEntry.key} costs low at RM ${String.format(Locale.US, "%.2f", lowestEntry.value)} so far."
        } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Budget Logic (Strictly Reactive for Current Month)
     */
    val budgetPredictionText: StateFlow<String> = combine(currentMonthTransactions, monthlyBudget) { list, budget ->
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

    val budgetUsagePercent: StateFlow<Int> = combine(currentMonthTransactions, monthlyBudget) { list, budget ->
        val currentSpend = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        ((currentSpend / budget) * 100).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentSpendAmount: StateFlow<Double> = currentMonthTransactions.map { list ->
        list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
