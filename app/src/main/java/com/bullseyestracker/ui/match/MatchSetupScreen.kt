package com.bullseyestracker.ui.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.model.GameMode

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 4

/**
 * Match setup (spec 003-501-match / 004-cricket-match User Story 1, mode selection moved to
 * [com.bullseyestracker.ui.home.GameModeListScreen] by spec 013-app-home-navigation): collect
 * 2-4 player names for the already-chosen [gameMode], blocking "Start match" outside that range
 * (FR-001, Acceptance Scenario 2). Cricket only requires a 2-player minimum in principle (004
 * spec Assumptions), but the shared `Match` model caps every mode at 4 players, so both modes use
 * the same bounds here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    gameMode: GameMode,
    onStartMatch: (GameMode, List<String>) -> Unit,
    onBack: () -> Unit,
) {
    val playerNames = remember { mutableStateListOf("", "") }

    val trimmedNames = playerNames.map { it.trim() }
    val canStart = trimmedNames.size in MIN_PLAYERS..MAX_PLAYERS && trimmedNames.all { it.isNotEmpty() }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text(if (gameMode == GameMode.FIVE_O_ONE) "New 501 match" else "New Cricket match")

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(playerNames.size) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = playerNames[index],
                        onValueChange = { playerNames[index] = it },
                        label = { Text("Player ${index + 1}") },
                        modifier = Modifier.fillMaxWidth(if (playerNames.size > MIN_PLAYERS) 0.85f else 1f),
                    )
                    if (playerNames.size > MIN_PLAYERS) {
                        IconButton(onClick = { playerNames.removeAt(index) }) {
                            Text("×")
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Button(
                onClick = { playerNames.add("") },
                enabled = playerNames.size < MAX_PLAYERS,
            ) { Text("Add player") }

            Button(
                onClick = { onStartMatch(gameMode, trimmedNames) },
                enabled = canStart,
            ) { Text("Start match") }
        }
    }
}
