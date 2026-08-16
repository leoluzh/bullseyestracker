package com.bullseyestracker.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.model.CricketNumber
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.Player
import com.bullseyestracker.ui.theme.DartGold

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

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface),
                    ),
                ),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Text("‹ Back", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (match.gameMode == GameMode.FIVE_O_ONE) "501 match" else "Cricket match",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "${winner?.name ?: "Unknown player"} won",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DartGold,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            match.players.forEach { player ->
                PlayerResultCard(
                    player = player,
                    gameMode = match.gameMode,
                    isWinner = player.id == match.winnerId,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PlayerResultCard(
    player: Player,
    gameMode: GameMode,
    isWinner: Boolean,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (isWinner) {
                    Box(
                        modifier =
                            Modifier
                                .background(DartGold.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "Winner",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DartGold,
                        )
                    }
                }
            }
            when (gameMode) {
                GameMode.FIVE_O_ONE ->
                    Text(
                        "Remaining: ${player.remainingScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                GameMode.CRICKET -> {
                    val marksSummary = CRICKET_NUMBERS.joinToString(" ") { "${it.pointValue}:${player.marks[it] ?: 0}" }
                    Text(
                        marksSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "Points: ${player.points}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
