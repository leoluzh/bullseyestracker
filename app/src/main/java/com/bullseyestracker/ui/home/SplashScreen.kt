package com.bullseyestracker.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Opening/branding screen (spec 013-app-home-navigation User Story 2): shown briefly on cold
 * start before the app auto-advances to [HomeScreen]. Purely presentational — the auto-advance
 * timer lives in the caller (`MainActivity`) since this composable has no navigation knowledge.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("BullseyesTracker", style = MaterialTheme.typography.headlineLarge)
    }
}
