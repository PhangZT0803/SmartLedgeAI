package com.user.smartledgerai.ui.screens.newtransaction

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.user.smartledgerai.data.TransactionType

@Composable
fun CounterPartySection(
    selectedType: TransactionType,
    toField: String,
    fromField: String,
    onToFieldChange: (String) -> Unit,
    onFromFieldChange: (String) -> Unit
) {
    when (selectedType) {
        TransactionType.SPENDING -> {
            SectionLabel("To (Merchant)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = toField,
                onValueChange = { onToFieldChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. GrabFood, KFC") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        TransactionType.INCOME -> {
            SectionLabel("From (Source)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = fromField,
                onValueChange = { onFromFieldChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Salary, Freelance") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        TransactionType.TRANSFER -> {
            SectionLabel("From (Account)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = fromField,
                onValueChange = { onFromFieldChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Maybank Savings") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(20.dp))

            SectionLabel("To (Account)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = toField,
                onValueChange = { onToFieldChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. GrabPay Wallet") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
    Spacer(Modifier.height(20.dp))
}
