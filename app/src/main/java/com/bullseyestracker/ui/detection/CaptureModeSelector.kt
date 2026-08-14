package com.bullseyestracker.ui.detection

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class CaptureMode { LIVE_CAMERA, PHOTO }

/**
 * Lets the player choose live-camera scoring (US1) vs single-photo scoring (US2) before
 * entering a turn — spec doesn't prescribe one as default, so the caller owns the initial value.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureModeSelector(
    selected: CaptureMode,
    onSelected: (CaptureMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.padding(8.dp)) {
        CaptureMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = CaptureMode.entries.size),
                selected = selected == mode,
                onClick = { onSelected(mode) },
            ) {
                Text(if (mode == CaptureMode.LIVE_CAMERA) "Live Camera" else "Photo")
            }
        }
    }
}
