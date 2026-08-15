package com.bullseyestracker.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.Match
import java.text.DateFormat
import java.util.Date

/**
 * Match history list (spec 005-match-history User Story 2): completed matches most-recent-first
 * (ordering already guaranteed by `MatchRepository.observeCompletedMatches()`), with an
 * empty-state message when none exist (FR-006). Tapping a row opens that match's detail via
 * [onMatchSelected]; [onBack] returns to the start screen (Acceptance Scenario 4).
 */
@Composable
fun MatchHistoryScreen(
    viewModel: HistoryViewModel,
    onMatchSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val matches by viewModel.completedMatches.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text(
            "Match history",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        if (matches.isEmpty()) {
            Text(
                "No completed matches yet.",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                items(matches, key = { it.id }) { match ->
                    MatchHistoryRow(match = match, onClick = { onMatchSelected(match.id) })
                }
            }
        }
    }
}

@Composable
private fun MatchHistoryRow(
    match: Match,
    onClick: () -> Unit,
) {
    val winnerName = match.players.firstOrNull { it.id == match.winnerId }?.name ?: "Unknown"
    val modeLabel = if (match.gameMode == GameMode.FIVE_O_ONE) "501" else "Cricket"
    val playerNames = match.players.joinToString(", ") { it.name }
    val dateLabel = match.endedAt?.let { DateFormat.getDateInstance().format(Date(it)) } ?: ""

    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$modeLabel — $playerNames", style = MaterialTheme.typography.bodyLarge)
            Text(dateLabel, style = MaterialTheme.typography.bodySmall)
        }
        Text("Winner: $winnerName", style = MaterialTheme.typography.bodyMedium)
    }
}
