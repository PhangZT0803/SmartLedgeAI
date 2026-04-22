package com.user.smartledgerai.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionType
import com.user.smartledgerai.data.Category
import com.user.smartledgerai.ui.theme.ExpenseColorDark
import com.user.smartledgerai.ui.theme.ExpenseColorLight
import com.user.smartledgerai.ui.theme.IncomeColorDark
import com.user.smartledgerai.ui.theme.IncomeColorLight
import com.user.smartledgerai.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortType(val label: String) {
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    AMOUNT_DESC("Amount (Highest)"),
    AMOUNT_ASC("Amount (Lowest)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactionViewModel: TransactionViewModel,
    onEditTransaction: (Transaction) -> Unit = {}
) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val allCategories by transactionViewModel.allCategories.collectAsState()
    val colors = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val expenseColor = if (isDark) ExpenseColorDark else ExpenseColorLight
    val incomeColor = if (isDark) IncomeColorDark else IncomeColorLight

    var selectedFilter by remember { mutableStateOf("All") }
    var sortType by remember { mutableStateOf(SortType.DATE_DESC) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val categoryMapping by transactionViewModel.categoryMapping.collectAsState()

    val filteredTransactions = remember(transactions, selectedFilter, sortType, categoryMapping) {
        val filtered = transactions.filter { transaction ->
            when (selectedFilter) {
                "All" -> true
                "Income" -> transaction.transactionType == TransactionType.INCOME
                "Expense" -> transaction.transactionType == TransactionType.SPENDING
                else -> (categoryMapping[transaction.categoryId] ?: "").equals(selectedFilter, true)
            }
        }
        
        when (sortType) {
            SortType.DATE_DESC -> filtered.sortedByDescending { it.timestamp }
            SortType.DATE_ASC -> filtered.sortedBy { it.timestamp }
            SortType.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            SortType.AMOUNT_ASC -> filtered.sortedBy { it.amount }
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Sort, 
                                    contentDescription = "Sort Options", 
                                    tint = colors.onBackground, 
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                SortType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.label) },
                                        onClick = { 
                                            sortType = type
                                            sortMenuExpanded = false
                                        },
                                        trailingIcon = {
                                            if (sortType == type) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Text("Transaction History", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            item { FilterChipsRow(colors, selectedFilter, allCategories) { selectedFilter = it } }
            item { Spacer(Modifier.height(4.dp)) }

            if (filteredTransactions.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { Text("No transactions found", color = colors.onSurfaceVariant) } }
            } else {
                items(filteredTransactions, key = { it.transactionId }) { transaction ->
                    val categoryName = categoryMapping[transaction.categoryId] ?: "Others"
                    SwipeToDeleteItem(
                        colors = colors,
                        transaction = transaction,
                        categoryName = categoryName,
                        expenseColor = expenseColor,
                        incomeColor = incomeColor,
                        onClick = { onEditTransaction(transaction) },
                        onDelete = {
                            transactionViewModel.deleteTransaction(transaction)
                            scope.launch { snackbarHostState.showSnackbar("Transaction deleted") }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteItem(colors: ColorScheme, transaction: Transaction, categoryName: String, expenseColor: Color, incomeColor: Color, onClick: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color(0xFFBA1A1A)).padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(28.dp)) }
        },
        content = { TransactionCard(colors, transaction, categoryName, expenseColor, incomeColor, onClick) }
    )
}

@Composable
private fun TransactionCard(colors: ColorScheme, transaction: Transaction, categoryName: String, expenseColor: Color, incomeColor: Color, onClick: () -> Unit) {
    val isIncome = transaction.transactionType == TransactionType.INCOME
    val icon = getCategoryIcon(categoryName)
    val dateString = remember(transaction.timestamp) {
        SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault()).format(Date(transaction.timestamp))
    }

    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, color = colors.surface, shape = RoundedCornerShape(20.dp), tonalElevation = 1.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(colors.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.merchant, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                Text(categoryName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = colors.secondary)
                Text(dateString, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Text(
                "${if (isIncome) "+ " else "- "}RM ${String.format("%,.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isIncome) incomeColor else expenseColor
            )
        }
    }
}

@Composable
private fun FilterChipsRow(colors: ColorScheme, selectedFilter: String, allCategories: List<Category>, onFilterSelected: (String) -> Unit) {
    // Dynamic filters based on database categories
    val baseFilters = listOf("All", "Income", "Expense")
    val dynamicFilters = allCategories.map { it.name }.distinct()
    val filters = baseFilters + dynamicFilters

    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                modifier = Modifier.clip(CircleShape).clickable { onFilterSelected(filter) },
                color = if (isSelected) colors.secondary else colors.surfaceVariant,
                contentColor = if (isSelected) colors.onSecondary else colors.onSurfaceVariant,
                shape = CircleShape
            ) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.onSecondary)
                    Text(filter, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
                }
            }
        }
    }
}

private fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "drinks" -> Icons.Default.LocalCafe
        "housing" -> Icons.Default.Home
        "salary" -> Icons.Default.AccountBalance
        "bills" -> Icons.Default.Receipt
        "freelance" -> Icons.Default.Work
        else -> Icons.Default.ReceiptLong
    }
}