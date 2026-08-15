package com.bullseyestracker.ui.match

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.MatchStatus

/**
 * 501 scoreboard (spec 003-501-match User Story 1/3): remaining score per player, whose turn is
 * active, and a winner banner once the match is [MatchStatus.COMPLETED] (FR-006). Turn
 * confirmation itself is blocked for a completed match by [MatchViewModel.confirmTurn] (FR-008);
 * this screen only reflects that state, it doesn't enforce it.
 */
@Composable
fun FiveOOneScoreboardScreen(
    match: Match,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        if (match.status == MatchStatus.COMPLETED) {
            val winner = match.players.firstOrNull { it.id == match.winnerId }
            Text(
                "${winner?.name ?: "Unknown player"} wins!",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        match.players.forEachIndexed { index, player ->
            val isActive = match.status == MatchStatus.IN_PROGRESS && index == match.currentPlayerIndex
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(if (isActive) "▶ ${player.name}" else player.name, modifier = Modifier.fillMaxWidth(0.7f))
                Text("${player.remainingScore}")
            }
        }
    }
}
