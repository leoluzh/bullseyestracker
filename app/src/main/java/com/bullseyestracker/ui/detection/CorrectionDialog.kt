package com.bullseyestracker.ui.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.cv.Ring
import com.bullseyestracker.cv.ScoreCalculator
import com.bullseyestracker.ui.theme.DartRed

/**
 * Lets the player correct a detected throw's sector/ring before it's committed to a match
 * score (spec FR-006, constitution Principle V). A manual correction is treated as fully
 * confident (confidence = 1f) — the caller is responsible for marking the resulting match
 * Throw as manually corrected.
 */
@Composable
fun CorrectionDialog(
    initial: DetectedThrow,
    onConfirm: (DetectedThrow) -> Unit,
    onDismiss: () -> Unit,
) {
    var sectorNumber by remember { mutableStateOf(initial.sectorNumber ?: 20) }
    var ring by remember { mutableStateOf(initial.ring) }
    val sectorApplies = ring != Ring.INNER_BULL && ring != Ring.OUTER_BULL && ring != Ring.MISS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { CorrectionDialogTitle(detectedValue = initial.value) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RingPicker(selected = ring, onSelected = { ring = it })
                if (sectorApplies) {
                    SectorPicker(selected = sectorNumber, onSelected = { sectorNumber = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val effectiveSector = if (sectorApplies) sectorNumber else null
                onConfirm(
                    initial.copy(
                        sectorNumber = effectiveSector,
                        ring = ring,
                        value = ScoreCalculator.valueFor(effectiveSector, ring),
                        confidence = 1f,
                    ),
                )
            }) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun CorrectionDialogTitle(detectedValue: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(DartRed, CircleShape))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                "Correct throw",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Detected as $detectedValue pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Ring.displayName(): String =
    when (this) {
        Ring.SINGLE -> "Single"
        Ring.DOUBLE -> "Double"
        Ring.TRIPLE -> "Treble"
        Ring.OUTER_BULL -> "Outer Bull"
        Ring.INNER_BULL -> "Bullseye"
        Ring.MISS -> "Miss"
    }

@Composable
private fun PickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text("⌄", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectorPicker(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PickerField(label = "Sector", value = selected.toString(), onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (1..20).forEach { number ->
                PickerMenuItem(
                    label = number.toString(),
                    isSelected = number == selected,
                    onClick = {
                        onSelected(number)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun RingPicker(
    selected: Ring,
    onSelected: (Ring) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PickerField(label = "Ring", value = selected.displayName(), onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Ring.entries.forEach { r ->
                PickerMenuItem(
                    label = r.displayName(),
                    isSelected = r == selected,
                    onClick = {
                        onSelected(r)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PickerMenuItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingIcon =
            if (isSelected) {
                { Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            } else {
                null
            },
        onClick = onClick,
    )
}
