package com.bullseyestracker.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Main-menu landing screen (spec 013-app-home-navigation User Story 1): the single entry point
 * for the app's four top-level features, replacing the old setup screen's bolted-on buttons.
 */
@Composable
fun HomeScreen(
    onNewGame: () -> Unit,
    onMatchHistory: () -> Unit,
    onPlayerStats: () -> Unit,
    onTestCalibrator: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("BullseyesTracker", style = MaterialTheme.typography.headlineMedium)

        Button(onClick = onNewGame, modifier = Modifier.padding(top = 24.dp)) {
            Text("New game")
        }
        Button(onClick = onMatchHistory, modifier = Modifier.padding(top = 8.dp)) {
            Text("Match history")
        }
        Button(onClick = onPlayerStats, modifier = Modifier.padding(top = 8.dp)) {
            Text("Player stats")
        }
        Button(onClick = onTestCalibrator, modifier = Modifier.padding(top = 8.dp)) {
            Text("Test calibrator")
        }
    }
}
