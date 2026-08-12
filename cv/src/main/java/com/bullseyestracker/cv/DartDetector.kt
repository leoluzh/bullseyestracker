package com.bullseyestracker.cv

/**
 * A dart found in a frame, in the same normalized 0..1 space as [BoardCalibration], before
 * [ScoreMapper] resolves it to sector/ring.
 */
data class RawDartDetection(
    val positionX: Float,
    val positionY: Float,
    val confidence: Float
)

/**
 * Finds darts stuck in the board given a prior [BoardCalibration]. Kept separate from
 * [BoardDetector] and [ScoreMapper] per constitution Principle II — see [BoardDetector] doc.
 */
interface DartDetector {
    fun detect(frame: FrameInput, calibration: BoardCalibration): List<RawDartDetection>
}
