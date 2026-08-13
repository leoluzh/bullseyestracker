package com.bullseyestracker.cv

import android.graphics.Bitmap

/**
 * Sole entry point the `app` and `match` modules use to reach vision functionality.
 * No OpenCV (or other CV-backend) type may cross this boundary — see
 * specs/001-dart-scoring-match/contracts/cv-engine-contract.md.
 */
interface CvEngine {
    fun calibrateBoard(frame: FrameInput): BoardCalibrationResult

    fun detectThrows(
        frame: FrameInput,
        calibration: BoardCalibration,
    ): List<DetectedThrow>
}

data class FrameInput(
    val bitmap: Bitmap,
    val rotationDegrees: Int = 0,
)

/**
 * Dartboard scoring geometry, normalized to 0..1 of the source frame's width/height so it's
 * independent of frame resolution and lets [CvEngineImpl] stay free of any Android/OpenCV
 * image-dimension APIs (kept fully unit-testable on a plain JVM).
 */
data class BoardCalibration(
    val centerX: Float,
    val centerY: Float,
    val innerBullRadius: Float,
    val outerBullRadius: Float,
    val tripleRingInnerRadius: Float,
    val tripleRingOuterRadius: Float,
    val doubleRingInnerRadius: Float,
    val doubleRingOuterRadius: Float,
    val rotationOffsetDegrees: Float,
)

sealed interface BoardCalibrationResult {
    data class Calibrated(val calibration: BoardCalibration, val confidence: Float) : BoardCalibrationResult

    data object NotFound : BoardCalibrationResult
}

enum class Ring { SINGLE, DOUBLE, TRIPLE, OUTER_BULL, INNER_BULL, MISS }

data class DetectedThrow(
    /** 1-20, null when [ring] is BULL/MISS (no sector applies). */
    val sectorNumber: Int?,
    val ring: Ring,
    val value: Int,
    val confidence: Float,
    /** Normalized 0..1 position within the frame (see [BoardCalibration]), for overlay rendering. */
    val boardPositionX: Float,
    val boardPositionY: Float,
)
