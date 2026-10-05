package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DisplayFiltersDialog(
    currentNgcMagnitudeLimit: Double,
    currentHideBelowHorizon: Boolean,
    onSave: (ngcMagnitudeLimit: Double, hideBelowHorizon: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var magnitudeText by remember { mutableStateOf("%.1f".format(currentNgcMagnitudeLimit)) }
    var hideBelowHorizon by remember { mutableStateOf(currentHideBelowHorizon) }
    val parsedMagnitude = magnitudeText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Display Filters") },
        text = {
            Column {
                Text(
                    "Only show NGC objects at or brighter than this magnitude (lower numbers are brighter). Objects with no published magnitude are always shown.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = magnitudeText,
                    onValueChange = { magnitudeText = it },
                    label = { Text("NGC limiting magnitude") },
                    singleLine = true,
                    isError = parsedMagnitude == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hideBelowHorizon, onCheckedChange = { hideBelowHorizon = it })
                    Text("Hide objects currently below the horizon")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { parsedMagnitude?.let { onSave(it, hideBelowHorizon) } },
                enabled = parsedMagnitude != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
