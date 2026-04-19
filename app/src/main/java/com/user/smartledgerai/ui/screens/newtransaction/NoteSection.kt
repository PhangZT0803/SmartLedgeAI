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

@Composable
fun NoteSection(note: String, onNoteChange: (String) -> Unit) {
    SectionLabel("Note")
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Optional") },
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(12.dp)
    )
    Spacer(Modifier.height(32.dp))
}
