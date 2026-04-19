package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(transactionViewModel: TransactionViewModel) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val colors = MaterialTheme.colorScheme
    val typo = MaterialTheme.typography

    // TODO: 之后从 ViewModel 拿月度统计
    val totalBalance = transactions.sumOf { tx ->
        when (tx.transactionType) {
            com.user.smartledgerai.data.TransactionType.INCOME -> tx.amount
            com.user.smartledgerai.data.TransactionType.SPENDING -> -tx.amount
            com.user.smartledgerai.data.TransactionType.TRANSFER -> 0.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SmartLedger AI",
                        style = typo.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = colors.primary
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Balance
            item { BalanceSection(totalBalance = totalBalance) }

            // Quick Stats
            item { QuickStatsRow(transactions = transactions) }

            // Recent Transactions
            item {
                Text(
                    text = "Recent Transactions",
                    style = typo.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transactions yet",
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(transactions.take(10)) { tx ->
                    TransactionRow(tx)
                }
            }
        }
    }
}

@Composable
private fun BalanceSection(totalBalance: Double) {
    val colors = MaterialTheme.colorScheme
    val typo = MaterialTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "TOTAL BALANCE",
            style = typo.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            ),
            color = colors.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "RM ",
                style = typo.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.primary.copy(alpha = 0.6f)
            )
            Text(
                text = String.format("%,.2f", totalBalance),
                style = typo.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.primary
            )
        }
    }
}

@Composable
private fun QuickStatsRow(transactions: List<Transaction>) {
    val income = transactions
        .filter { it.transactionType == com.user.smartledgerai.data.TransactionType.INCOME }
        .sumOf { it.amount }
    val spending = transactions
        .filter { it.transactionType == com.user.smartledgerai.data.TransactionType.SPENDING }
        .sumOf { it.amount }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Income Card
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Income",
            amount = income,
            icon = Icons.Default.AccountBalanceWallet,
            isHighlight = false
        )
        // Spending Card
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Spending",
            amount = spending,
            icon = Icons.Default.TrendingDown,
            isHighlight = true
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlight: Boolean
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (isHighlight) colors.primary else colors.surfaceVariant
    val contentColor = if (isHighlight) colors.onPrimary else colors.onSurface

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
                Text(
                    text = "RM ${String.format("%,.2f", amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    val colors = MaterialTheme.colorScheme
    val isSpending = tx.transactionType == com.user.smartledgerai.data.TransactionType.SPENDING
    val dateStr = remember(tx.timestamp) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
    }

    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(44.dp),
                color = colors.surfaceVariant,
                shape = CircleShape
            ) {
                Icon(
                    if (isSpending) Icons.Default.ArrowOutward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Merchant + date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.merchant,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = "${if (isSpending) "-" else "+"} RM ${String.format("%,.2f", tx.amount)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isSpending)
                    com.user.smartledgerai.ui.theme.ExpenseColorLight
                else
                    com.user.smartledgerai.ui.theme.IncomeColorLight
            )
        }
    }
}