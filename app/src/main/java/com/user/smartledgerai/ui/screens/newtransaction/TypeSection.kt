package com.user.smartledgerai.ui.screens.newtransaction

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.user.smartledgerai.data.TransactionType

@Composable
fun TypeSection(selectedType: TransactionType, onTypeChange: (TransactionType) -> Unit) {
    SectionLabel("Type")
    Spacer(Modifier.height(8.dp))

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TransactionType.entries.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeChange(type) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = TransactionType.entries.size
                )
            ) {
                Text(type.name)
            }
        }
    }

    Spacer(Modifier.height(20.dp))
}
