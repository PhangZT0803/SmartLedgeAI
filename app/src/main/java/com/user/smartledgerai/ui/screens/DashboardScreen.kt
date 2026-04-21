package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.ui.theme.AiGradient
import com.user.smartledgerai.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(transactionViewModel: TransactionViewModel,onAction:()->Unit){
    val transactions by transactionViewModel.transactions.collectAsState()
    val verifiedTransactions = transactions.filter { it.isVerified }

    val colors = MaterialTheme.colorScheme
    val typo = MaterialTheme.typography

    // TODO: 之后从 ViewModel 拿月度统计
    val totalBalance = verifiedTransactions.sumOf { tx ->
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
            item { ImmersiveBalanceCard(totalBalance = totalBalance) }

            // Quick Stats
            item { QuickStatsRow(verifiedTransactions = verifiedTransactions) }

            // Recent Transactions
            item {
                // 1. 用一个 Row 处理头部，让文字和按钮左右分布
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // 左右对齐
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = typo.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    TextButton(onClick = { onAction() }) {
                        Text("View All")
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowOutward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

// 2. 列表项依然直接放在 LazyColumn 下，保持纵向排列
            if (verifiedTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions yet", color = colors.onSurfaceVariant)
                    }
                }
            } else {
                items(verifiedTransactions.take(10)) { tx ->
                    TransactionRow(tx) // 每一条记录会自动换行显示
                }
            }
        }
    }
}

@Composable
private fun ImmersiveBalanceCard(totalBalance: Double) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = colors.primary,
                ambientColor = colors.secondary
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AiGradient) // 使用你定义的 AI 渐变色
                .padding(24.dp)
        ) {
            // 背景装饰：半透明的大图标
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 24.dp)
                    .graphicsLayer(alpha = 0.1f, rotationZ = -15f),
                tint = Color.White
            )

            Column {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "RM ",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = String.format("%,.2f", totalBalance),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(verifiedTransactions: List<Transaction>) {
    val income = verifiedTransactions
        .filter { it.transactionType == com.user.smartledgerai.data.TransactionType.INCOME }
        .sumOf { it.amount }
    val spending = verifiedTransactions
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
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                color = if(isSpending) colors.errorContainer else colors.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    if (isSpending) Icons.Default.ArrowOutward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if(isSpending) colors.error else colors.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Merchant + date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.merchant,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // Amount
            Text(
                text = "${if (isSpending) "-" else "+"} RM ${String.format("%,.2f", tx.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSpending)
                    com.user.smartledgerai.ui.theme.ExpenseColorLight
                else
                    com.user.smartledgerai.ui.theme.IncomeColorLight
            )
        }
    }
}