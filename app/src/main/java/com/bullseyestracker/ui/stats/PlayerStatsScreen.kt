package com.bullseyestracker.ui.stats

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
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
import com.bullseyestracker.match.stats.PlayerStats
import com.bullseyestracker.ui.home.DartboardEmblem
import com.bullseyestracker.ui.theme.DartGold
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

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
                "Player stats",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Win rate by player, most successful first",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

            if (stats.isEmpty()) {
                EmptyStatsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(stats, key = { it.name }) { entry -> PlayerStatsCard(entry) }
                }
            }
        }
    }
}

@Composable
private fun EmptyStatsState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DartboardEmblem(size = 56.dp)
        Spacer(Modifier.height(16.dp))
        Text(
            "No statistics yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            "Finish a match to see win rates here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun PlayerStatsCard(entry: PlayerStats) {
    val winPercent = (entry.winRate * 100).toInt()
    val accent =
        when {
            entry.winRate >= 0.5f -> DartGreen
            entry.winRate > 0f -> DartGold
            else -> DartRed
        }

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
                Column {
                    Text(
                        entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "${entry.matchesWon}/${entry.matchesPlayed} won",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                WinRateBadge(percent = winPercent, accent = accent)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { entry.winRate },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = accent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun WinRateBadge(
    percent: Int,
    accent: Color,
) {
    Box(
        modifier =
            Modifier
                .background(accent.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            "$percent%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}
