package com.bullseyestracker.ui.detection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bullseyestracker.cv.DetectedThrow

private const val LOW_CONFIDENCE_THRESHOLD = 0.4f
private val MARKER_SIZE = 32.dp

/**
 * Draws each detection as a tappable marker at its normalized board position (FR-005), tinted
 * to flag low-confidence detections (FR-012). Tapping a marker opens [CorrectionDialog] via
 * [onDetectionTapped].
 */
@Composable
fun DetectionOverlay(
    detections: List<DetectedThrow>,
    modifier: Modifier = Modifier,
    onDetectionTapped: (DetectedThrow) -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        detections.forEach { detection ->
            val isLowConfidence = detection.confidence < LOW_CONFIDENCE_THRESHOLD
            val xDp = with(density) { (detection.boardPositionX * widthPx).toDp() } - (MARKER_SIZE / 2)
            val yDp = with(density) { (detection.boardPositionY * heightPx).toDp() } - (MARKER_SIZE / 2)

            androidx.compose.foundation.layout.Box(
                modifier =
                    Modifier
                        .offset(x = xDp, y = yDp)
                        .size(MARKER_SIZE)
                        .clip(CircleShape)
                        .background(if (isLowConfidence) Color(0xCCFFC107) else Color(0xCC00E676))
                        .clickable { onDetectionTapped(detection) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = detection.value.toString(),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
