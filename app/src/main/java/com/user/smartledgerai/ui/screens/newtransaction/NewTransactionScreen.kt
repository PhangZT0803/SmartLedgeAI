package com.user.smartledgerai.ui.screens.newtransaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.user.smartledgerai.data.Transaction
import com.user.smartledgerai.data.TransactionType
import com.user.smartledgerai.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    transactionViewModel: TransactionViewModel,
    isEditMode: Boolean,
    transactionToEdit: Transaction?,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    // ── State ──
    var amount by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var currency by remember { mutableStateOf(transactionToEdit?.currency ?: "RM") }
    var selectedType by remember { mutableStateOf(transactionToEdit?.transactionType ?: TransactionType.SPENDING) }

    var toField by remember { mutableStateOf(transactionToEdit?.merchant ?: "") }
    var fromAccount by remember { mutableStateOf(transactionToEdit?.source ?: "") }

    val categories by transactionViewModel.getCategoriesByType(selectedType).collectAsState(initial = emptyList())
    var selectedCategoryId by remember { mutableStateOf(transactionToEdit?.categoryId ?: -1) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = transactionToEdit?.timestamp ?: System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val displayDate = dateFormatter.format(Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis()))

    var note by remember { mutableStateOf(transactionToEdit?.description ?: "") }

    // ── UI ──
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Transaction" else "New Transaction")
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AmountSection(
                amount = amount,
                currency = currency,
                onAmountChange = { amount = it },
                onCurrencyChange = { currency = it }
            )
            TypeSection(selectedType, onTypeChange = { selectedType = it })
            CounterPartySection(
                selectedType = selectedType,
                toField = toField,
                fromField = fromAccount,
                onToFieldChange = { toField = it },
                onFromFieldChange = { fromAccount = it }
            )
            AnimatedVisibility(visible = selectedType != TransactionType.TRANSFER) {
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

            Button(
                onClick = { onSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
