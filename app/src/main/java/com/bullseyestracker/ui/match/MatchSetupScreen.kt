package com.bullseyestracker.ui.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val MIN_PLAYERS = 2
private const val MAX_PLAYERS = 4

/**
 * 501 match setup (spec 003-501-match User Story 1): collect 2-4 player names, blocking
 * "Start match" outside that range (FR-001, Acceptance Scenario 2).
 */
@Composable
fun MatchSetupScreen(onStartMatch: (List<String>) -> Unit) {
    val playerNames = remember { mutableStateListOf("", "") }

    val trimmedNames = playerNames.map { it.trim() }
    val canStart = trimmedNames.size in MIN_PLAYERS..MAX_PLAYERS && trimmedNames.all { it.isNotEmpty() }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("New 501 match")

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
                onClick = { onStartMatch(trimmedNames) },
                enabled = canStart,
            ) { Text("Start match") }
        }
    }
}
