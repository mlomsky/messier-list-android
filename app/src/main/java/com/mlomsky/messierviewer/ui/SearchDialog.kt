package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mlomsky.messierviewer.astro.CompassPoints
import com.mlomsky.messierviewer.viewmodel.ObjectSearchFilter

@Composable
fun SearchDialog(
    current: ObjectSearchFilter,
    onApply: (ObjectSearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(current.nameQuery) }
    var type by remember { mutableStateOf(current.typeQuery) }
    var direction by remember { mutableStateOf(current.direction) }
    var minAltitudeText by remember { mutableStateOf(current.minAltitudeDeg?.let { "%.0f".format(it) } ?: "") }
    val minAltitudeValid = minAltitudeText.isBlank() || minAltitudeText.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Search Objects") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name contains") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type contains (e.g. Galaxy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Direction in sky", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    CompassPoints.take(4).forEach { point ->
                        DirectionChip(point, direction == point, Modifier.weight(1f).padding(horizontal = 2.dp)) {
                            direction = if (direction == point) null else point
                        }
                    }
                }
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    CompassPoints.drop(4).forEach { point ->
                        DirectionChip(point, direction == point, Modifier.weight(1f).padding(horizontal = 2.dp)) {
                            direction = if (direction == point) null else point
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = minAltitudeText,
                    onValueChange = { minAltitudeText = it },
                    label = { Text("Minimum altitude (°)") },
                    singleLine = true,
                    isError = !minAltitudeValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        ObjectSearchFilter(
                            nameQuery = name.trim(),
                            typeQuery = type.trim(),
                            direction = direction,
                            minAltitudeDeg = minAltitudeText.toDoubleOrNull()
                        )
                    )
                },
                enabled = minAltitudeValid
            ) {
                Text("Search")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onApply(ObjectSearchFilter()) }) { Text("Clear") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
private fun DirectionChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) }
    )
}
