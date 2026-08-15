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
import com.bullseyestracker.match.model.CricketNumber
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.match.model.Player

private val NUMBERS = CricketNumber.entries

private fun markSymbol(count: Int): String =
    when {
        count <= 0 -> "-"
        count == 1 -> "/"
        count == 2 -> "X"
        else -> "⊗"
    }

private fun numberLabel(number: CricketNumber): String = if (number == CricketNumber.BULL) "B" else number.pointValue.toString()

/**
 * Cricket scoreboard (spec 004-cricket-match User Story 1/3): a marks grid (15-20 + bull) and
 * points per player, active-player indicator, and a winner banner once the match is
 * [MatchStatus.COMPLETED] (FR-009). Turn confirmation itself is blocked for a completed match by
 * [MatchViewModel.confirmTurn] (FR-010); this screen only reflects that state.
 */
@Composable
fun CricketScoreboardScreen(
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

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("", modifier = Modifier.weight(2f))
            NUMBERS.forEach { number -> Text(numberLabel(number), modifier = Modifier.weight(1f)) }
            Text("Pts", modifier = Modifier.weight(1f))
        }

        match.players.forEachIndexed { index, player ->
            val isActive = match.status == MatchStatus.IN_PROGRESS && index == match.currentPlayerIndex
            PlayerMarksRow(player = player, isActive = isActive)
        }
    }
}

@Composable
private fun PlayerMarksRow(
    player: Player,
    isActive: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(if (isActive) "▶ ${player.name}" else player.name, modifier = Modifier.weight(2f))
        NUMBERS.forEach { number ->
            Text(
                markSymbol(player.marks[number] ?: 0),
                modifier = Modifier.weight(1f),
            )
        }
        Text("${player.points}", modifier = Modifier.weight(1f))
    }
}
