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
import com.bullseyestracker.match.model.GameMode

/**
 * Game-mode selection screen (spec 013-app-home-navigation User Story 3): shown before player
 * setup so the mode is picked once here rather than via a control embedded in
 * [com.bullseyestracker.ui.match.MatchSetupScreen].
 */
@Composable
fun GameModeListScreen(
    onModeSelected: (GameMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text(
            "Choose a game mode",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        GameMode.entries.forEach { mode ->
            Button(
                onClick = { onModeSelected(mode) },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(if (mode == GameMode.FIVE_O_ONE) "501" else "Cricket")
            }
        }
    }
}
