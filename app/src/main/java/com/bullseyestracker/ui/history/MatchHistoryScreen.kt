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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.ui.home.DartboardEmblem
import com.bullseyestracker.ui.theme.DartGold
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed
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
                "Match history",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Your completed matches, most recent first",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            if (matches.isEmpty()) {
                EmptyHistoryState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(matches, key = { it.id }) { match ->
                        MatchHistoryRow(match = match, onClick = { onMatchSelected(match.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DartboardEmblem(size = 56.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            "No completed matches yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            "Finish a game to see it here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun MatchHistoryRow(
    match: Match,
    onClick: () -> Unit,
) {
    val winnerName = match.players.firstOrNull { it.id == match.winnerId }?.name ?: "Unknown"
    val modeLabel = if (match.gameMode == GameMode.FIVE_O_ONE) "501" else "Cricket"
    val modeAccent = if (match.gameMode == GameMode.FIVE_O_ONE) DartRed else DartGreen
    val playerNames = match.players.joinToString(", ") { it.name }
    val dateLabel = match.endedAt?.let { DateFormat.getDateInstance().format(Date(it)) } ?: ""

    ElevatedCard(
        onClick = onClick,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModeBadge(label = modeLabel, accent = modeAccent)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        playerNames,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "Winner: $winnerName",
                style = MaterialTheme.typography.bodyMedium,
                color = DartGold,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ModeBadge(
    label: String,
    accent: Color,
) {
    Box(
        modifier =
            Modifier
                .background(accent.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}
