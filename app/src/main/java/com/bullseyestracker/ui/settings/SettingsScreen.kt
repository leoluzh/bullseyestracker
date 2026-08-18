package com.bullseyestracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bullseyestracker.cv.DetectionBackend

/**
 * Detection-backend switch (spec 014-dnn-dart-detection User Story 2). Selecting an option calls
 * [onBackendSelected] immediately — the caller is expected to apply it to the live [CvEngine][
 * com.bullseyestracker.cv.CvEngine] and persist it (see `MainActivity`), so it takes effect on
 * the very next capture without an app restart (spec SC-002).
 */
@Composable
fun SettingsScreen(
    selectedBackend: DetectionBackend,
    isDnnAvailable: Boolean,
    onBackendSelected: (DetectionBackend) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(24.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("‹ Home", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(8.dp))
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text(
            "Detection backend",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "Classical uses OpenCV shape detection. DNN uses a trained object-detection model, " +
                "which may score better in tricky lighting but is newer and less proven.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        BackendOption(
            title = "Classical",
            subtitle = "OpenCV shape detection (default)",
            selected = selectedBackend == DetectionBackend.CLASSICAL,
            enabled = true,
            onClick = { onBackendSelected(DetectionBackend.CLASSICAL) },
        )
        Spacer(Modifier.height(12.dp))
        BackendOption(
            title = "DNN",
            subtitle = if (isDnnAvailable) "Trained object-detection model" else "Unavailable on this device",
            selected = selectedBackend == DetectionBackend.DNN,
            enabled = isDnnAvailable,
            onClick = { onBackendSelected(DetectionBackend.DNN) },
        )
    }
}

@Composable
private fun BackendOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, enabled = enabled, onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 6.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick, enabled = enabled)
            Spacer(Modifier.height(0.dp))
            Column(Modifier.padding(start = 8.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
