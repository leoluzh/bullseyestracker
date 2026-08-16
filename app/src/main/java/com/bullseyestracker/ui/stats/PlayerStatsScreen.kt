package com.bullseyestracker.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.stats.PlayerStats

/**
 * Player statistics list (spec 010-player-stats): win rate per player name, most-successful
 * first (ordering already guaranteed by `PlayerStatsCalculator.compute`), with an empty-state
 * message when no completed matches exist yet (FR-005).
 */
@Composable
fun PlayerStatsScreen(
    viewModel: PlayerStatsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats by viewModel.playerStats.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text(
            "Player stats",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        if (stats.isEmpty()) {
            Text(
                "No statistics yet — finish a match to see win rates here.",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                items(stats, key = { it.name }) { entry -> PlayerStatsRow(entry) }
            }
        }
    }
}

@Composable
private fun PlayerStatsRow(entry: PlayerStats) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(entry.name, modifier = Modifier.fillMaxWidth(0.4f))
        Text("${entry.matchesWon}/${entry.matchesPlayed} won", modifier = Modifier.fillMaxWidth(0.6f))
        Text("${(entry.winRate * 100).toInt()}%")
    }
}
