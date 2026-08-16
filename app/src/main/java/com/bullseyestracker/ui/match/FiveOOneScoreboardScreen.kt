package com.bullseyestracker.ui.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.match.model.Player
import com.bullseyestracker.ui.theme.DartGold

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

            match.players.forEachIndexed { index, player ->
                val isActive = match.status == MatchStatus.IN_PROGRESS && index == match.currentPlayerIndex
                PlayerScoreRow(player = player, isActive = isActive)
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
private fun PlayerScoreRow(
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                player.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            "${player.remainingScore}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
