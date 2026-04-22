package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionType
import com.user.smartledgerai.ui.theme.ExpenseColorDark
import com.user.smartledgerai.ui.theme.ExpenseColorLight
import com.user.smartledgerai.ui.theme.IncomeColorDark
import com.user.smartledgerai.ui.theme.IncomeColorLight
import com.user.smartledgerai.viewmodel.TransactionViewModel

data class CategoryData(val name: String, val percentage: Float, val color: Color, val icon: ImageVector)

// Donut chart palette
private val ChartColors = listOf(
    Color(0xFF14B8A6), Color(0xFF0891B2), Color(0xFFF59E0B),
    Color(0xFF818CF8), Color(0xFFF43F5E), Color(0xFF10B981)
)

private val CategoryIcons = mapOf(
    "food" to Icons.Default.Restaurant,
    "transport" to Icons.Default.DirectionsCar,
    "shopping" to Icons.Default.ShoppingBag,
    "drinks" to Icons.Default.LocalCafe,
    "housing" to Icons.Default.Home,
    "salary" to Icons.Default.AccountBalance,
    "bills" to Icons.Default.Receipt,
    "freelance" to Icons.Default.Work
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    transactionViewModel: TransactionViewModel,
    onAction: () -> Unit = {},
    onInsights: () -> Unit = {}
) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val expenseColor = if (isDark) ExpenseColorDark else ExpenseColorLight
    val incomeColor = if (isDark) IncomeColorDark else IncomeColorLight

    val verifiedTransactions = transactions.filter { it.isVerified }
    val categoryMapping by transactionViewModel.categoryMapping.collectAsState()

    val totalBalance = verifiedTransactions.sumOf { if (it.transactionType == TransactionType.INCOME) it.amount else -it.amount }
    val income = verifiedTransactions.filter { it.transactionType == TransactionType.INCOME }.sumOf { it.amount }
    val spending = verifiedTransactions.filter { it.transactionType == TransactionType.SPENDING }.sumOf { it.amount }

    // Dynamic category breakdown from real categoryId
    val categoryBreakdown = remember(verifiedTransactions, categoryMapping) {
        val spendingTransactions = verifiedTransactions.filter { it.transactionType == TransactionType.SPENDING }
        val totalSpend = spendingTransactions.sumOf { it.amount }
        if (totalSpend == 0.0) {
            listOf(CategoryData("No Data", 100f, Color.Gray, Icons.Default.Payments))
        } else {
            val groups = mutableMapOf<String, Double>()
            spendingTransactions.forEach { transaction ->
                val catName = categoryMapping[transaction.categoryId] ?: "Others"
                groups[catName] = (groups[catName] ?: 0.0) + transaction.amount
            }
            groups.entries.mapIndexed { index, (name, amount) ->
                val pct = ((amount / totalSpend) * 100).toFloat()
                val icon = CategoryIcons[name.lowercase()] ?: Icons.Default.Payments
                val color = ChartColors[index % ChartColors.size]
                CategoryData(name, pct, color, icon)
            }.sortedByDescending { it.percentage }
        }
    }

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("SmartLedger AI", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = colors.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { BalanceHeader(colors, totalBalance) }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MonthlyStatCard(Modifier.weight(1f), "Monthly Income", income, colors.surfaceVariant, colors.primary, Icons.Default.AccountBalanceWallet)
                    MonthlyStatCard(Modifier.weight(1f), "Monthly Spend", spending, colors.primary, colors.onPrimary, Icons.Default.TrendingDown)
                }
            }

            item { AiGoalBanner(colors, onInsights) }

            item { SpendingCategorySection(colors, categoryBreakdown, spending) }

            item { SectionHeader(colors, "Recent Transactions") { onAction() } }

            if (verifiedTransactions.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("No verified transactions yet", color = colors.onSurfaceVariant) } }
            } else {
                items(verifiedTransactions.take(5)) { transaction ->
                    VerifiedTxCard(colors, transaction, categoryMapping, incomeColor, expenseColor)
                }
            }
        }
    }
}

@Composable
private fun BalanceHeader(colors: ColorScheme, balance: Double) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("TOTAL BALANCE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = colors.onSurfaceVariant)
            Surface(color = colors.secondaryContainer, shape = CircleShape) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(colors.secondary, CircleShape))
                    Text("AI SYNCING...", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.secondary)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("RM", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp, end = 4.dp))
            Text(String.format("%,.2f", balance), style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black, fontSize = 42.sp), color = colors.onBackground)
        }
    }
}

@Composable
private fun MonthlyStatCard(modifier: Modifier, label: String, amount: Double, containerColor: Color, contentColor: Color, icon: ImageVector) {
    Surface(modifier = modifier, color = containerColor, shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = contentColor.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
            Text("RM ${String.format("%,.2f", amount)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
        }
    }
}

@Composable
private fun AiGoalBanner(colors: ColorScheme, onClick: () -> Unit) {
    Surface(color = colors.errorContainer, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = colors.error, modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("AI GOAL PREDICTION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.onErrorContainer.copy(alpha = 0.7f))
                Text("Careful! You're RM 984 over your planned budget today.", style = MaterialTheme.typography.bodySmall, color = colors.onErrorContainer)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onErrorContainer)
        }
    }
}

@Composable
private fun SpendingCategorySection(colors: ColorScheme, categories: List<CategoryData>, totalSpend: Double) {
    Surface(color = colors.surface, shape = RoundedCornerShape(24.dp), tonalElevation = 2.dp) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(20.dp))
                Text("Spending by Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
            }
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                    DynamicDonutChart(categories)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL SPEND", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp), color = colors.onSurfaceVariant)
                        Text(String.format("%,.0f", totalSpend), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSurface)
                    }
                }
                Spacer(Modifier.width(24.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    categories.forEach { LegendItem(colors, it) }
                }
            }
        }
    }
}

@Composable
private fun DynamicDonutChart(categories: List<CategoryData>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 24.dp.toPx()
        val totalPct = categories.sumOf { it.percentage.toDouble() }.toFloat()
        var startAngle = -90f
        categories.forEach { cat ->
            val sweep = (cat.percentage / totalPct) * 360f
            drawArc(color = cat.color, startAngle = startAngle, sweepAngle = sweep, useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            startAngle += sweep
        }
    }
}

@Composable
private fun LegendItem(colors: ColorScheme, category: CategoryData) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(36.dp).background(category.color.copy(alpha = 0.2f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(category.name.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = colors.onSurfaceVariant)
            Text("${category.percentage.toInt()}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSurface)
        }
    }
}

@Composable
private fun SectionHeader(colors: ColorScheme, title: String, onSeeAll: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.onBackground)
        TextButton(onClick = onSeeAll) { Text("See All", color = colors.secondary, style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun VerifiedTxCard(colors: ColorScheme, transaction: Transaction, categoryMapping: Map<Int, String>, incomeColor: Color, expenseColor: Color) {
    val isIncome = transaction.transactionType == TransactionType.INCOME
    val catName = categoryMapping[transaction.categoryId] ?: "Others"

    Surface(color = colors.surface, shape = RoundedCornerShape(24.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(colors.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                Icon(CategoryIcons[catName.lowercase()] ?: Icons.Default.ReceiptLong, contentDescription = null, tint = colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.merchant, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(14.dp))
                    Text(catName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = colors.secondary)
                }
            }
            Text("${if (isIncome) "+ " else "- "}RM ${String.format("%,.2f", transaction.amount)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = if (isIncome) incomeColor else expenseColor)
        }
    }
}