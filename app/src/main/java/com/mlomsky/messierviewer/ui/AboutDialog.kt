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
import com.mlomsky.messierviewer.BuildConfig
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val aboutDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "WAA Presents\nViewing Tonight",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIconBadgeFillWidth()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Version ${BuildConfig.VERSION_NAME}  •  ${LocalDate.now().format(aboutDateFormatter)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Created by Michael Lomsky for the Westchester Amateur Astronomers",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "An observing tool to help you pick what objects you'd like to see among the Messier, NGC, and planet catalogs.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("What's new in v2.2", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    val latestChanges = listOf(
                        "Rebranded as \"WAA Presents Viewing Tonight\" for the Westchester Amateur Astronomers",
                        "Added the Moon to the Planets list",
                        "The limiting-magnitude filter now applies to Planets and Messier too, not just NGC",
                        "P / M / NGC catalog buttons shortened so labels stop getting cut off on some phones"
                    )
                    latestChanges.forEach { change ->
                        Text("•  $change", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("What's new in v2.1", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    val newChanges = listOf(
                        "Tap any object's name for an elevation chart across tonight's session",
                        "Tap Sunset/Sunrise/Moonrise/Moonset/Illumination for a Sun & Moon altitude chart with twilight and night shading"
                    )
                    newChanges.forEach { change ->
                        Text("•  $change", style = MaterialTheme.typography.bodySmall)
                    }
                }
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
