package com.user.smartledgerai.ui.screens.verify

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    pendingTransactions: List<Transaction>,
    transactionViewModel: TransactionViewModel,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    onEditManually: (Transaction) -> Unit = {}
) {
    val currentTransaction = pendingTransactions.firstOrNull()
    val colors = MaterialTheme.colorScheme
    val allCategories by transactionViewModel.allCategories.collectAsState()

    if (currentTransaction == null) {
        EmptyVerificationScreen(onBack)
        return
    }

    // Look up real category name from DB
    val categoryLabel = allCategories.find { it.id == currentTransaction.categoryId }?.name ?: "Others"
    val categoryIcon = getCategoryIcon(categoryLabel)

    Scaffold(
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("Verification", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.onBackground) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.onBackground) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section Header
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Verify Record", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), color = colors.secondary)
                Text("Confirm the AI's interpretation of your recent spend.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }

            // Raw Source Card
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("RAW SOURCE DATA", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp), color = colors.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(currentTransaction.rawData ?: "No raw data available", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = colors.onSurfaceVariant)
                }
            }

                Surface(
                    color = colors.surface,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp).background(colors.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(categoryIcon, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(36.dp))
                        }

                        Spacer(Modifier.height(20.dp))
                        Text(currentTransaction.merchant, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSurface, textAlign = TextAlign.Center)
                        Text(categoryLabel.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = colors.secondary, modifier = Modifier.padding(top = 4.dp))

                        Spacer(Modifier.height(24.dp))
                        Box(modifier = Modifier.width(48.dp).height(3.dp).background(colors.outlineVariant.copy(alpha = 0.3f), CircleShape))
                        Spacer(Modifier.height(24.dp))

                        Text("DETECTED AMOUNT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp), color = colors.onSurfaceVariant)
                        Text("RM ${String.format("%,.2f", currentTransaction.amount)}", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.secondary)
                    }
                }

                // Bottom accent bar
                Box(
                    modifier = Modifier.fillMaxWidth().height(5.dp)
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(Brush.horizontalGradient(listOf(colors.primary, colors.secondary, colors.primary)))
                )

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                Button(
                    onClick = {
                        transactionViewModel.verifyTransaction(
                            original = currentTransaction,
                            amount = currentTransaction.amount,
                            currency = currentTransaction.currency,
                            merchant = currentTransaction.merchant,
                            source = currentTransaction.source,
                            categoryId = currentTransaction.categoryId,
                            timestamp = currentTransaction.timestamp,
                            description = currentTransaction.description
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Confirm & Save", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.onSecondary)
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.onSecondary)
                    }
                }

                OutlinedButton(
                    onClick = { onEditManually(currentTransaction) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.outlineVariant)
                ) {
                    Text("Edit Manually", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
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
private fun EmptyVerificationScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("All caught up!", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = colors.onBackground)
            Text("No pending AI records.", color = colors.onSurfaceVariant)
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)) {
                Text("Back to Dashboard", color = colors.onSecondary)
            }
        }
    }
}