package com.bullseyestracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

/**
 * Game-mode selection screen (spec 013-app-home-navigation User Story 3): shown before player
 * setup so the mode is picked once here rather than via a control embedded in
 * [com.bullseyestracker.ui.match.MatchSetupScreen].
 */
@Composable
fun GameModeListScreen(
    onModeSelected: (GameMode) -> Unit,
    onBack: () -> Unit,
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
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
                Text("‹ Back", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Choose a game mode",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "Pick how you want to play",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
            )

            MenuCard(
                title = "501",
                subtitle = "First to zero, doubles to finish",
                accent = DartRed,
                onClick = { onModeSelected(GameMode.FIVE_O_ONE) },
            )
            Spacer(Modifier.height(12.dp))
            MenuCard(
                title = "Cricket",
                subtitle = "Close 15–20 and bull, then score",
                accent = DartGreen,
                onClick = { onModeSelected(GameMode.CRICKET) },
            )
        }
    }
}
