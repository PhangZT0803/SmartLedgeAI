package com.user.smartledgerai.ui.screens.newtransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.user.smartledgerai.ui.theme.BrandingGradient

@Composable
fun AmountSection(amount: String, currency: String, onAmountChange: (String) -> Unit, onCurrencyChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typo = MaterialTheme.typography

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandingGradient)
            .padding(top=48.dp,bottom = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TRANSACTION AMOUNT",
                style = typo.labelSmall.copy(letterSpacing = 2.sp),
                color = colors.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CurrencyChip(
                    selected = currency,
                    onSelected = onCurrencyChange
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Allow only numbers and a single decimal point
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onAmountChange(newValue)
                        }
                    },
                    textStyle = typo.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = colors.onPrimaryContainer
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "0.00",
                            style = typo.headlineMedium.copy(
                                textAlign = TextAlign.Center,
                                color = colors.onPrimaryContainer.copy(alpha = 0.3f)
                            )
                        )
                    },
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))
}

/** Currency chip with dropdown */
@Composable
private fun CurrencyChip(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("RM", "USD", "SGD")
    var expanded by remember { mutableStateOf(false) }

    AssistChip(
        onClick = { expanded = true },
        label = {
            Text(
                selected,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        shape = RoundedCornerShape(10.dp)
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option) },
                onClick = {
                    onSelected(option)
                    expanded = false
                }
            )
        }
    }
}
