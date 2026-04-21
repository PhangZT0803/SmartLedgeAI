package com.user.smartledgerai.ui.screens.verify

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionType
import com.user.smartledgerai.ui.screens.newtransaction.AmountSection
import com.user.smartledgerai.ui.screens.newtransaction.CategorySection
import com.user.smartledgerai.ui.screens.newtransaction.CounterPartySection
import com.user.smartledgerai.ui.screens.newtransaction.DateSection
import com.user.smartledgerai.ui.screens.newtransaction.NoteSection
import com.user.smartledgerai.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(
    pendingTransactions: List<Transaction>,
    transactionViewModel: TransactionViewModel,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    // ── 当前正在验证第几条 ──
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentTransaction = pendingTransactions.getOrNull(currentIndex)

    if (currentTransaction == null) {
        EmptyVerificationScreen()
        return
    }

    // ── 从 Transaction entity 读取初始值，用户可修改 ──
    var amount by remember(currentIndex) { mutableStateOf(currentTransaction.amount.toString()) }
    var currency by remember(currentIndex) { mutableStateOf(currentTransaction.currency) }
    var selectedType by remember(currentIndex) { mutableStateOf(currentTransaction.transactionType) }
    var toField by remember(currentIndex) { mutableStateOf(currentTransaction.merchant) }
    var fromAccount by remember(currentIndex) { mutableStateOf(currentTransaction.source) }
    var selectedCategoryId by remember(currentIndex) { mutableIntStateOf(currentTransaction.categoryId) }
    var note by remember(currentIndex) { mutableStateOf(currentTransaction.description ?: "") }
    var showRawData by remember(currentIndex) { mutableStateOf(false) }

    val categories by transactionViewModel.getCategoriesByType(selectedType)
        .collectAsState(initial = emptyList())

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentTransaction.timestamp
    )
    var showDatePicker by remember(currentIndex) { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val displayDate = dateFormatter.format(
        Date(datePickerState.selectedDateMillis ?: currentTransaction.timestamp)
    )
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Verify Transaction", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${currentIndex + 1} / ${pendingTransactions.size} pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── AI 来源标识（只有 rawData 不为空时才显示）──
            if (currentTransaction.rawData != null) {
                AITrustBadge(source = currentTransaction.source)
                Spacer(Modifier.height(16.dp))
            }

            // ── 复用 newtransaction 的 Section 组件 ──
            AmountSection(
                amount = amount,
                currency = currency,
                onAmountChange = { amount = it },
                onCurrencyChange = { currency = it }
            )

            // Verify 不显示 TypeSection — AI 已确定类型，只读展示
            TypeIndicator(type = selectedType)

            CounterPartySection(
                selectedType = selectedType,
                toField = toField,
                fromField = fromAccount,
                onToFieldChange = { toField = it },
                onFromFieldChange = { fromAccount = it }
            )

            if (selectedType != TransactionType.TRANSFER) {
                CategorySection(categories, selectedCategoryId, onCategoryChange = { selectedCategoryId = it })
            }

            DateSection(
                displayDate = displayDate,
                showDatePicker = showDatePicker,
                datePickerState = datePickerState,
                onShowDatePicker = { showDatePicker = true },
                onDismissDatePicker = { showDatePicker = false }
            )

            NoteSection(note = note, onNoteChange = { note = it })

            // ── 原始通知数据 ──
            if (currentTransaction.rawData != null) {
                RawMetadataSection(
                    rawData = currentTransaction.rawData,
                    expanded = showRawData,
                    onToggle = { showRawData = !showRawData }
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── 操作按钮 ──
            Button(
                onClick = {
                    if(selectedCategoryId == -1 && selectedType != TransactionType.TRANSFER){
                        Toast.makeText(context,"Please select a category",Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val parsedAmount = amount.toDoubleOrNull() ?: return@Button
                    transactionViewModel.verifyTransaction(
                        original = currentTransaction,
                        amount = parsedAmount,
                        currency = currency,
                        merchant = toField,
                        source = fromAccount,
                        categoryId = selectedCategoryId,
                        timestamp = datePickerState.selectedDateMillis ?: currentTransaction.timestamp,
                        description = note.ifBlank { null }
                    )
                    if (currentIndex < pendingTransactions.size - 1) {
                        currentIndex++
                    } else {
                        onSave() //处理完回去主页面
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (currentIndex < pendingTransactions.size - 1) "Confirm & Next"
                    else "Confirm & Done",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // 跳过按钮
            if (pendingTransactions.size > 1 && currentIndex < pendingTransactions.size - 1) {
                OutlinedButton(
                    onClick = { currentIndex++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Skip")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** 只读的类型指示，不可切换 */
@Composable
private fun TypeIndicator(type: TransactionType) {
    val label = when (type) {
        TransactionType.SPENDING -> "Spending"
        TransactionType.INCOME -> "Income"
        TransactionType.TRANSFER -> "Transfer"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Type",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyVerificationScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No pending AI records.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}