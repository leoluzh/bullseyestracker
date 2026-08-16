package com.bullseyestracker.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.model.CricketNumber
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.match.model.Player
import com.bullseyestracker.ui.theme.DartGold

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
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (match.status == MatchStatus.COMPLETED) {
                val winner = match.players.firstOrNull { it.id == match.winnerId }
                WinnerBanner(name = winner?.name ?: "Unknown player")
                Spacer(Modifier.height(8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("", modifier = Modifier.weight(2f))
                NUMBERS.forEach { number ->
                    Text(
                        numberLabel(number),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    "Pts",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            match.players.forEachIndexed { index, player ->
                val isActive = match.status == MatchStatus.IN_PROGRESS && index == match.currentPlayerIndex
                PlayerMarksRow(player = player, isActive = isActive)
            }
        }
    }
}

@Composable
private fun WinnerBanner(name: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(DartGold.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp),
    ) {
        Text(
            "$name wins!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DartGold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PlayerMarksRow(
    player: Player,
    isActive: Boolean,
) {
    val rowBackground = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(rowBackground, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            player.name,
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
        )
        NUMBERS.forEach { number ->
            Text(
                markSymbol(player.marks[number] ?: 0),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "${player.points}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
