package com.bullseyestracker.ui.detection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

private val HALO_COLOR = Color.White.copy(alpha = 0.9f)
private const val STROKE_WIDTH_PX = 5f
private const val HALO_WIDTH_PX = STROKE_WIDTH_PX + 4f

/**
 * Draws the calibrated bullseye's inner/outer boundary as two concentric circle outlines (spec
 * 012-bullseye-overlay) — a visualization of [BoardCalibration] data already computed by
 * `CvEngine`, not a new detection algorithm (FR-005). Draws nothing when [calibration] is null
 * (no board calibrated yet, or calibration was lost, FR-003).
 */
@Composable
fun BullseyeOverlay(
    calibration: BoardCalibration?,
    modifier: Modifier = Modifier,
) {
    if (calibration == null) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerOffset = Offset(calibration.centerX * size.width, calibration.centerY * size.height)
        // Radii are normalized against frame width only (OpenCvBoardDetector's own doc comment,
        // a documented simplification assuming a roughly square capture region) — converting
        // back with size.width keeps this overlay in the exact same coordinate space
        // DetectionOverlay's markers and ScoreMapper's hit-testing already assume.
        val radiusScale = size.width
        val outerRadius = calibration.outerBullRadius * radiusScale
        val innerRadius = calibration.innerBullRadius * radiusScale

        // A white halo behind each colored ring keeps the boundary legible against any camera
        // background, matching DetectionOverlay's marker-border treatment.
        drawCircle(color = HALO_COLOR, radius = outerRadius, center = centerOffset, style = Stroke(width = HALO_WIDTH_PX))
        drawCircle(color = HALO_COLOR, radius = innerRadius, center = centerOffset, style = Stroke(width = HALO_WIDTH_PX))
        drawCircle(color = DartGreen, radius = outerRadius, center = centerOffset, style = Stroke(width = STROKE_WIDTH_PX))
        drawCircle(color = DartRed, radius = innerRadius, center = centerOffset, style = Stroke(width = STROKE_WIDTH_PX))
    }
}
