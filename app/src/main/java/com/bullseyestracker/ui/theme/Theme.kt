package com.bullseyestracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DartRed = Color(0xFFE5383B)
val DartGreen = Color(0xFF2E7D46)
val DartGold = Color(0xFFE8B33D)
val BoardBlack = Color(0xFF121417)

private val DarkColors =
    darkColorScheme(
        primary = DartRed,
        onPrimary = Color(0xFFFFFFFF),
        secondary = DartGreen,
        onSecondary = Color(0xFFFFFFFF),
        tertiary = DartGold,
        onTertiary = Color(0xFF1A1400),
        background = BoardBlack,
        onBackground = Color(0xFFEDEDED),
        surface = Color(0xFF1C1F24),
        onSurface = Color(0xFFEDEDED),
        surfaceVariant = Color(0xFF272B31),
        onSurfaceVariant = Color(0xFFA9AFB8),
        outline = Color(0xFF4A5058),
    )

@Composable
fun BullseyesTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
