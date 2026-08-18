package com.bullseyestracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bullseyestracker.ui.theme.DartGold
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

/**
 * Main-menu landing screen (spec 013-app-home-navigation User Story 1): the single entry point
 * for the app's four top-level features, replacing the old setup screen's bolted-on buttons.
 */
@Composable
fun HomeScreen(
    onNewGame: () -> Unit,
    onMatchHistory: () -> Unit,
    onPlayerStats: () -> Unit,
    onTestCalibrator: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))
            DartboardEmblem(size = 76.dp)
            Spacer(Modifier.height(20.dp))
            Text(
                "BullseyesTracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                "Auto-scored darts, powered by your camera",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 40.dp),
            )

            MenuCard(
                title = "New game",
                subtitle = "Start a 501 or Cricket match",
                accent = DartRed,
                onClick = onNewGame,
            )
            Spacer(Modifier.height(12.dp))
            MenuCard(
                title = "Match history",
                subtitle = "Review past matches and results",
                accent = DartGreen,
                onClick = onMatchHistory,
            )
            Spacer(Modifier.height(12.dp))
            MenuCard(
                title = "Player stats",
                subtitle = "Win rates and scoring averages",
                accent = DartGold,
                onClick = onPlayerStats,
            )
            Spacer(Modifier.height(12.dp))
            MenuCard(
                title = "Test calibrator",
                subtitle = "Check board detection and alignment",
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onTestCalibrator,
            )
            Spacer(Modifier.height(12.dp))
            MenuCard(
                title = "Settings",
                subtitle = "Choose the detection backend",
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onSettings,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
