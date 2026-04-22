package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.user.smartledgerai.data.TransactionType
import com.user.smartledgerai.viewmodel.TransactionViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    transactionViewModel: TransactionViewModel,
    onBack: () -> Unit = {}
) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val colors = MaterialTheme.colorScheme

    var monthlyBudget by remember { mutableDoubleStateOf(3000.0) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // Real-time spending: recalculates on EVERY transaction change
    val monthlySpending = remember(transactions, currentMonth, currentYear) {
        transactions.filter { transaction ->
            transaction.isVerified && transaction.transactionType == TransactionType.SPENDING &&
            Calendar.getInstance().apply { timeInMillis = transaction.timestamp }.let {
                it.get(Calendar.MONTH) == currentMonth && it.get(Calendar.YEAR) == currentYear
            }
        }
    }

    val totalSpend = monthlySpending.sumOf { it.amount }
    val budgetPct = ((totalSpend / monthlyBudget) * 100).coerceIn(0.0, 999.0).toInt()
    val isOverBudget = totalSpend > monthlyBudget
    val diffAmount = kotlin.math.abs(totalSpend - monthlyBudget)

    // Smart category analysis using REAL categoryId from DB
    val categoryMapping by transactionViewModel.categoryMapping.collectAsState()
    val categorySpend = remember(monthlySpending, categoryMapping) {
        val groups = mutableMapOf<String, Double>()
        monthlySpending.forEach { transaction ->
            val catName = categoryMapping[transaction.categoryId] ?: "Others"
            groups[catName] = (groups[catName] ?: 0.0) + transaction.amount
        }
        groups
    }
    val highestCat = categorySpend.maxByOrNull { it.value }
    val lowestCat = if (categorySpend.size > 1) categorySpend.minByOrNull { it.value } else null

    // Dynamic weekly spending for line chart
    val weeklySpending = remember(monthlySpending) {
        val weeks = mutableListOf(0.0, 0.0, 0.0, 0.0)
        monthlySpending.forEach { transaction ->
            val weekIdx = Calendar.getInstance().apply { timeInMillis = transaction.timestamp }.let {
                ((it.get(Calendar.DAY_OF_MONTH) - 1) / 7).coerceIn(0, 3)
            }
            weeks[weekIdx] += transaction.amount
        }
        weeks
    }

    if (showBudgetDialog) {
        BudgetEditDialog(colors, monthlyBudget, { showBudgetDialog = false }, { monthlyBudget = it; showBudgetDialog = false })
    }

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("AI Insights", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.onBackground) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.onBackground) } },
                actions = { IconButton(onClick = { showBudgetDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = colors.onBackground) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            BudgetGauge(colors, budgetPct, totalSpend, monthlyBudget)
            BudgetStatusPill(colors, isOverBudget, diffAmount)
            AiAnalysisCards(colors, highestCat, lowestCat)
            SpendingTrendLineChart(colors, weeklySpending)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BudgetGauge(colors: ColorScheme, percentage: Int, spent: Double, budget: Double) {
    val arcColor = colors.primary
    val trackColor = colors.outlineVariant.copy(alpha = 0.3f)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset((size.width - 2 * radius) / 2, (size.height - 2 * radius) / 2)
                val arcSize = Size(radius * 2, radius * 2)

                drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                val sweep = (percentage.coerceAtMost(100) / 100f) * 360f
                drawArc(color = arcColor, startAngle = -90f, sweepAngle = sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$percentage%", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 44.sp), color = colors.onBackground)
                Text("BUDGET USED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp), color = colors.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("RM ${String.format("%,.0f", spent)} / RM ${String.format("%,.0f", budget)}", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun BudgetStatusPill(colors: ColorScheme, isOver: Boolean, amount: Double) {
    Surface(
        color = if (isOver) colors.errorContainer else colors.secondaryContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(if (isOver) Icons.Default.Warning else Icons.Default.TrendingUp, contentDescription = null, tint = if (isOver) colors.error else colors.secondary, modifier = Modifier.size(18.dp))
            Text(
                if (isOver) "Careful! You are RM ${String.format("%,.0f", amount)} over your planned budget today."
                else "You are RM ${String.format("%,.0f", amount)} ahead of your goal.",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (isOver) colors.onErrorContainer else colors.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AiAnalysisCards(colors: ColorScheme, highest: Map.Entry<String, Double>?, lowest: Map.Entry<String, Double>?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.onBackground, modifier = Modifier.size(24.dp))
            Text("AI Analysis", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.onBackground)
        }

        // Smart Alert
        Surface(color = colors.secondaryContainer, shape = RoundedCornerShape(24.dp)) {
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                Surface(color = colors.secondary.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
                    Box(modifier = Modifier.padding(10.dp)) { Icon(getCategoryIcon(highest?.key), contentDescription = null, tint = colors.secondary) }
                }
                Column {
                    Text("Smart Alert", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSecondaryContainer)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (highest != null) "Your ${highest.key} expenses are high at RM ${String.format("%,.2f", highest.value)}. Consider scaling back this week."
                        else "Keep tracking your expenses for AI insights.",
                        style = MaterialTheme.typography.bodySmall, color = colors.onSecondaryContainer.copy(alpha = 0.8f), lineHeight = 20.sp
                    )
                }
            }
        }

        // Great Job
        Surface(color = colors.surface, shape = RoundedCornerShape(24.dp), tonalElevation = 2.dp) {
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                Surface(color = colors.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) {
                    Box(modifier = Modifier.padding(10.dp)) { Icon(getCategoryIcon(lowest?.key), contentDescription = null, tint = colors.primary) }
                }
                Column {
                    Text("Great Job", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (lowest != null) "Great job! You kept ${lowest.key} costs low at RM ${String.format("%,.2f", lowest.value)} so far."
                        else "Add more transactions to see personalized tips.",
                        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SpendingTrendLineChart(colors: ColorScheme, weeklyData: List<Double>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Spending Trend", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.onBackground)

        Surface(color = colors.surface, shape = RoundedCornerShape(24.dp), tonalElevation = 2.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                val labels = listOf("W1", "W2", "W3", "W4")
                val maxVal = weeklyData.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
                val lineColor = colors.primary
                val dotColor = colors.primary
                val gridColor = colors.outlineVariant.copy(alpha = 0.2f)
                val textColor = colors.onSurfaceVariant

                // Line Chart
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 32.dp)) {
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val stepX = chartWidth / (weeklyData.size - 1).coerceAtLeast(1)

                        // Grid lines
                        for (i in 0..3) {
                            val y = chartHeight * (i / 3f)
                            drawLine(gridColor, Offset(0f, y), Offset(chartWidth, y), strokeWidth = 1.dp.toPx())
                        }

                        // Points
                        val points = weeklyData.mapIndexed { index, value ->
                            val x = index * stepX
                            val y = chartHeight - (value / maxVal).toFloat() * chartHeight
                            Offset(x, y.coerceIn(0f, chartHeight))
                        }

                        // Fill area under the line
                        if (points.size >= 2) {
                            val fillPath = Path().apply {
                                moveTo(points.first().x, chartHeight)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, chartHeight)
                                close()
                            }
                            drawPath(fillPath, lineColor.copy(alpha = 0.1f))
                        }

                        // Line segments
                        for (i in 0 until points.size - 1) {
                            drawLine(lineColor, points[i], points[i + 1], strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                        }

                        // Dots
                        points.forEach { point ->
                            drawCircle(dotColor, radius = 6.dp.toPx(), center = point)
                            drawCircle(Color.White, radius = 3.dp.toPx(), center = point)
                        }
                    }

                    // X Labels
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labels.forEach { label ->
                            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor)
                        }
                    }
                }

                // Legend
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    weeklyData.forEachIndexed { i, v ->
                        if (v > 0) {
                            Text("${labels[i]}: RM ${String.format("%,.0f", v)}", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String?): ImageVector {
    return when (category?.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "drinks" -> Icons.Default.LocalCafe
        "housing" -> Icons.Default.Home
        "salary" -> Icons.Default.AccountBalance
        "bills" -> Icons.Default.Receipt
        else -> Icons.Default.Payments
    }
}

@Composable
private fun BudgetEditDialog(colors: ColorScheme, currentBudget: Double, onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var budgetText by remember { mutableStateOf(String.format("%.0f", currentBudget)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant,
        title = { Text("Set Monthly Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter your target monthly budget (RM):", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("RM ", color = colors.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface, unfocusedTextColor = colors.onSurface,
                        focusedBorderColor = colors.primary, unfocusedBorderColor = colors.outline, cursorColor = colors.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { budgetText.toDoubleOrNull()?.let { if (it > 0) onConfirm(it) } }) { Text("Save", color = colors.primary, fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.onSurfaceVariant) } }
    )
}
