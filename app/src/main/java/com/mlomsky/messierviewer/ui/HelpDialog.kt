package com.mlomsky.messierviewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Help") },
        text = {
            Box {
                Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(scrollState)) {
                    HelpItem(
                        "Observing session",
                        "Every calculation in this app is based on one observing session: 6pm local time tonight to 6am the next morning. If you open the app between midnight and 6am, you're seeing the tail end of the session that started the evening before."
                    )
                    HelpItem(
                        "Location",
                        "Uses your phone's GPS by default (falls back to New York City if that's unavailable). Tap the location icon to search for a different city or address instead."
                    )
                    HelpItem(
                        "Planets / Messier / NGC buttons",
                        "Independently turn each catalog on or off. NGC adds thousands of fainter deep-sky objects beyond the 110 Messier objects — pair it with the magnitude limit below to keep the list manageable."
                    )
                    HelpItem(
                        "Search (magnifying glass)",
                        "Filter the current list by name, object type (e.g. \"Galaxy\"), compass direction, and/or a minimum current altitude. All fields are optional and combine together; \"Clear\" resets them."
                    )
                    HelpItem(
                        "Display Filters (tune icon)",
                        "Set the dimmest magnitude NGC objects to include (lower numbers are brighter; objects with no published magnitude are always shown), and optionally hide any object that's currently below the horizon, across all catalogs."
                    )
                    HelpItem(
                        "Sunrise/sunset & moon",
                        "Shown even if they fall slightly outside the 6pm-6am session, since the search window is widened a bit around it. A missing moonrise or moonset on a given night is expected sometimes — it matches real published moon tables, not a bug."
                    )
                    HelpItem(
                        "Night Mode",
                        "The moon icon switches all text to red to preserve your night vision while observing."
                    )
                    HelpItem(
                        "Sort buttons",
                        "Sort the object list by Name, Max Elevation during the session, Rise/Set Time, Now (only objects currently above the horizon, sorted by current altitude), or the ★ Favorites toggle (tap again to turn it off)."
                    )
                    HelpItem(
                        "Object list",
                        "Every object from the catalogs you've turned on: Messier, NGC, and the naked-eye/telescope planets. Current altitude/azimuth and compass direction sit next to each name, turning red (grey in Night Mode) once the object drops to or below the horizon. Max elevation and rise/set time for the session are on the right."
                    )
                    HelpItem(
                        "Magnitude",
                        "\"Mag\" is an object's apparent (visual) magnitude — lower numbers mean brighter. For planets this is a representative mean value, since their real brightness varies with position."
                    )
                    HelpItem(
                        "★ Favorites",
                        "Tap the star next to any object to favorite it, on any catalog. Use the ★ sort button to view just your favorites."
                    )
                    HelpItem(
                        "F button",
                        "Tap the F circle next to any object to record which filter works best for it (UHC, OIII, H-Beta, Light Pollution, or your own write-in). Several Messier objects already come with a starting recommendation."
                    )
                }
                AnimatedVisibility(
                    visible = scrollState.canScrollForward,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "More below",
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun HelpItem(title: String, body: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodySmall)
    }
    Spacer(modifier = Modifier.height(10.dp))
}
