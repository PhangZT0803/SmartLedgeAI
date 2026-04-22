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
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionType
import com.user.smartledgerai.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(transactionViewModel: TransactionViewModel) {
    val transactions by transactionViewModel.transactions.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    var sortLatestFirst by remember { mutableStateOf(true) }   // ← New: Toggle state

    // Apply filter + sorting
    val filteredTransaction = remember(selectedFilter, sortLatestFirst, transactions) {
        val filtered = when (selectedFilter) {
            "Income" -> transactions.filter { it.transactionType == TransactionType.INCOME }
            "Expense" -> transactions.filter { it.transactionType == TransactionType.SPENDING }
            "Transfer" ->transactions.filter { it.transactionType == TransactionType.TRANSFER }
            else -> transactions
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
                items(filteredTransaction) { tx ->
                    HistoryTransactionItem(tx)
                }

                if (filteredTransaction.isEmpty()) {
                    item {
                        Text(
                            "No transaction found",
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
fun HistoryTransactionItem(tx: Transaction) {
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
                color = if (tx.transactionType == TransactionType.INCOME) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    Icons.Default.Coffee,
                    contentDescription = null,
                    tint = if (tx.transactionType == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tx.merchant, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text( tx.categoryId.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text( tx.timestamp.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }

            Text(
                text = tx.amount.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (tx.transactionType == TransactionType.SPENDING) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}