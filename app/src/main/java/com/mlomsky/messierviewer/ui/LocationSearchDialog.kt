package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun LocationSearchDialog(
    isSearching: Boolean,
    errorMessage: String?,
    onSearch: (String) -> Unit,
    onUseGps: () -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Location") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("City or address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let { Text(it) }
            }
        },
        confirmButton = {
            Button(onClick = { onSearch(query) }, enabled = query.isNotBlank() && !isSearching) {
                Text(if (isSearching) "Searching…" else "Search")
            }
        },
        dismissButton = {
            TextButton(onClick = onUseGps) { Text("Use GPS") }
        }
    )
}
