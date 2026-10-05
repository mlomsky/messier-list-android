package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mlomsky.messierviewer.data.buildFilterText
import com.mlomsky.messierviewer.data.parseFilterText

@Composable
fun FilterEditDialog(
    objectName: String,
    currentFilterText: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parsed = remember(currentFilterText) { parseFilterText(currentFilterText) }
    var uhc by remember { mutableStateOf(parsed.uhc) }
    var oiii by remember { mutableStateOf(parsed.oiii) }
    var hBeta by remember { mutableStateOf(parsed.hBeta) }
    var lightPollution by remember { mutableStateOf(parsed.lightPollution) }
    var otherChecked by remember { mutableStateOf(parsed.otherChecked) }
    var otherText by remember { mutableStateOf(parsed.otherText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter - $objectName") },
        text = {
            Column {
                FilterCheckboxRow("UHC - Narrowband", uhc) { uhc = it }
                FilterCheckboxRow("OIII", oiii) { oiii = it }
                FilterCheckboxRow("H-Beta", hBeta) { hBeta = it }
                FilterCheckboxRow("Light Pollution - Wideband", lightPollution) { lightPollution = it }
                FilterCheckboxRow("Other", otherChecked) { otherChecked = it }
                if (otherChecked) {
                    OutlinedTextField(
                        value = otherText,
                        onValueChange = { otherText = it },
                        label = { Text("Other filter") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(buildFilterText(uhc, oiii, hBeta, lightPollution, otherChecked, otherText)) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun FilterCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
