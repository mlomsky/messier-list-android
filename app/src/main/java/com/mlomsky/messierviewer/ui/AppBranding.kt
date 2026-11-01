package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mlomsky.messierviewer.R

/** Width / height of the source WAA logo artwork (1004x496), used to size it without letterboxing. */
private const val WAA_LOGO_ASPECT_RATIO = 1004f / 496f

/** The Westchester Amateur Astronomers club logo, sized to a fixed height (e.g. the splash screen). */
@Composable
fun AppIconBadge(height: Dp = 72.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.waa_logo),
        contentDescription = "Westchester Amateur Astronomers",
        contentScale = ContentScale.Fit,
        modifier = modifier.fillMaxWidth().height(height)
    )
}

/**
 * The WAA logo, filling the available width at its natural aspect ratio. Unlike [AppIconBadge]'s
 * fixed height, this never reserves empty letterbox space, so it's the right choice inside a
 * width-constrained, height-limited container like a dialog.
 */
@Composable
fun AppIconBadgeFillWidth(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.waa_logo),
        contentDescription = "Westchester Amateur Astronomers",
        contentScale = ContentScale.Fit,
        modifier = modifier.fillMaxWidth().aspectRatio(WAA_LOGO_ASPECT_RATIO)
    )
}
