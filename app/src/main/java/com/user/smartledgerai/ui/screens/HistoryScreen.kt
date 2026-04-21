package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class HistoryTransaction(
    val title: String,
    val amount: String,
    val time: String,
    val category: String,
    val icon: ImageVector,
    val isIncome: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val baseTransactions = remember {
        listOf(
            HistoryTransaction("Bubble Tea - CoCo", "- RM 18.90", "Today, 02:30 PM", "Drinks", Icons.Default.LocalCafe),
            HistoryTransaction("Groceries - AEON", "- RM 150.00", "Today, 10:45 AM", "Food", Icons.Default.LocalGroceryStore),
            HistoryTransaction("Salary Deposit", "+ RM 4,200.00", "Today, 09:15 AM", "Income", Icons.Default.AccountBalance, true),
            HistoryTransaction("Starbucks Coffee", "- RM 24.50", "Yesterday", "Drinks", Icons.Default.Coffee),
            HistoryTransaction("Petrol - Shell", "- RM 100.00", "Yesterday", "Transport", Icons.Default.LocalGasStation),
            HistoryTransaction("Grab Ride to Campus", "- RM 35.00", "Apr 18", "Transport", Icons.Default.DirectionsCar),
            HistoryTransaction("McDonald's", "- RM 42.80", "Apr 15", "Food", Icons.Default.Fastfood),
            HistoryTransaction("Freelance Payment", "+ RM 850.00", "Apr 17", "Income", Icons.Default.Work, true),
            HistoryTransaction("Shopping - Uniqlo", "- RM 280.00", "Apr 16", "Shopping", Icons.Default.ShoppingBag),
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var sortLatestFirst by remember { mutableStateOf(true) }   // ← New: Toggle state

    // Apply filter + sorting
    val filteredTransactions = remember(selectedFilter, sortLatestFirst, baseTransactions) {
        val filtered = when (selectedFilter) {
            "Income" -> baseTransactions.filter { it.isIncome }
            "Expense" -> baseTransactions.filter { !it.isIncome }
            "Food" -> baseTransactions.filter { it.category == "Food" }
            "Drinks" -> baseTransactions.filter { it.category == "Drinks" }
            "Transport" -> baseTransactions.filter { it.category == "Transport" }
            "Shopping" -> baseTransactions.filter { it.category == "Shopping" }
            else -> baseTransactions
        }

        if (sortLatestFirst) filtered else filtered.reversed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { sortLatestFirst = !sortLatestFirst }) {
                        Icon(
                            imageVector = if (sortLatestFirst)
                                Icons.Default.ArrowDownward
                            else Icons.Default.ArrowUpward,
                            contentDescription = "Toggle Sort Order"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FilterChipsRow(selectedFilter) { newFilter ->
                selectedFilter = newFilter
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTransactions) { tx ->
                    HistoryTransactionItem(tx)
                }

                if (filteredTransactions.isEmpty()) {
                    item {
                        Text(
                            "No transactions found",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "Income", "Expense", "Food", "Drinks", "Transport", "Shopping")

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
fun HistoryTransactionItem(tx: HistoryTransaction) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (tx.isIncome) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    tx.icon,
                    contentDescription = null,
                    tint = if (tx.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(tx.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text(tx.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }

            Text(
                text = tx.amount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (tx.isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}