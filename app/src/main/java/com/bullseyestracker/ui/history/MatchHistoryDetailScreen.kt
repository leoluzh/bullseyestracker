package com.bullseyestracker.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.model.CricketNumber
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.Player

private val CRICKET_NUMBERS = CricketNumber.entries

/**
 * Match history detail (spec 005-match-history User Story 2): a single completed match's final
 * per-player result (FR-005) — 501 remaining score or Cricket marks/points, matching the display
 * conventions already used by `FiveOOneScoreboardScreen`/`CricketScoreboardScreen` — and the
 * recorded winner. [onBack] returns to the history list (Acceptance Scenario 4).
 */
@Composable
fun MatchHistoryDetailScreen(
    match: Match,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val winner = match.players.firstOrNull { it.id == match.winnerId }

    Column(modifier = modifier.padding(16.dp)) {
        Button(onClick = onBack) { Text("Back") }

        Text(
            if (match.gameMode == GameMode.FIVE_O_ONE) "501 match" else "Cricket match",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text("${winner?.name ?: "Unknown player"} won", style = MaterialTheme.typography.titleMedium)

        match.players.forEach { player -> PlayerResultRow(player = player, gameMode = match.gameMode) }
    }
}

@Composable
private fun PlayerResultRow(
    player: Player,
    gameMode: GameMode,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(player.name, modifier = Modifier.fillMaxWidth(0.4f))
        when (gameMode) {
            GameMode.FIVE_O_ONE -> Text("Remaining: ${player.remainingScore}")
            GameMode.CRICKET -> {
                val marksSummary = CRICKET_NUMBERS.joinToString(" ") { "${it.pointValue}:${player.marks[it] ?: 0}" }
                Text("$marksSummary — Points: ${player.points}")
            }
        }
    }
}
