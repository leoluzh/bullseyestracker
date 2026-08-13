package com.bullseyestracker.ui.detection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.cv.Ring
import com.bullseyestracker.cv.ScoreCalculator

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
        title = { Text("Correct throw") },
        text = {
            Column {
                RingPicker(selected = ring, onSelected = { ring = it })
                if (sectorApplies) {
                    SectorPicker(selected = sectorNumber, onSelected = { sectorNumber = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val effectiveSector = if (sectorApplies) sectorNumber else null
                onConfirm(
                    initial.copy(
                        sectorNumber = effectiveSector,
                        ring = ring,
                        value = ScoreCalculator.valueFor(effectiveSector, ring),
                        confidence = 1f,
                    ),
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SectorPicker(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row {
        Text("Sector: ", modifier = Modifier)
        Text(
            text = selected.toString(),
            modifier = Modifier.clickable { expanded = true },
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        (1..20).forEach { number ->
            DropdownMenuItem(
                text = { Text(number.toString()) },
                onClick = {
                    onSelected(number)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun RingPicker(
    selected: Ring,
    onSelected: (Ring) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row {
        Text("Ring: ")
        Text(
            text = selected.name,
            modifier = Modifier.clickable { expanded = true },
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        Ring.values().forEach { r ->
            DropdownMenuItem(
                text = { Text(r.name) },
                onClick = {
                    onSelected(r)
                    expanded = false
                },
            )
        }
    }
}
