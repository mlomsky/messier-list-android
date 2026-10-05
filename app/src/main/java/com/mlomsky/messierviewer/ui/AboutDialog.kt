package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val aboutDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Messier Tonight") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIconBadge()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Version 2  •  ${LocalDate.now().format(aboutDateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Created by Michael Lomsky", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "A Messier object and planet visibility tracker for tonight's observing session.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("What's new in v2", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    val changes = listOf(
                        "Added the NGC catalog (4,000+ deep-sky objects), alongside Messier and planets",
                        "Planets, Messier, and NGC can each be toggled on or off",
                        "New Search: filter the list by name, type, direction, or altitude",
                        "New Display Filters: set an NGC brightness limit and hide objects below the horizon",
                        "New Favorites: star any object and sort by favorites",
                        "The filter-recommendation button now works on every object, not just Messier",
                        "Apparent magnitude now shown for Messier, NGC, and planets",
                        "Help guide updated to cover all of the above"
                    )
                    changes.forEach { change ->
                        Text("•  $change", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
