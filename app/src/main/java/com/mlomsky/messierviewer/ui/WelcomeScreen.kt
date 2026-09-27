package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SplashBackground = Color(0xFF0B1026)
private val SplashForeground = Color(0xFFF5F1E3)

/** Shown only while the very first location/astronomy data load is in flight. */
@Composable
fun WelcomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppIconBadge(size = 96.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Messier Tonight",
                style = MaterialTheme.typography.headlineMedium,
                color = SplashForeground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Created by Michael Lomsky",
                style = MaterialTheme.typography.bodyMedium,
                color = SplashForeground
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = SplashForeground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Finding your sky…",
                style = MaterialTheme.typography.bodySmall,
                color = SplashForeground
            )
        }
    }
}
