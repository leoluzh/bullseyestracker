package com.bullseyestracker.cv.opencv.dnn

import com.bullseyestracker.cv.BoardCalibration
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Derives a [BoardCalibration] from the 4 DeepDarts calibration-point detections (research.md
 * §3), so the DNN backend is a drop-in for [com.bullseyestracker.cv.opencv.OpenCvBoardDetector]
 * as far as [com.bullseyestracker.cv.ScoreMapper] is concerned. Requires exactly one detection
 * per calibration-point class (1..4) -- callers must validate this first (data-model.md,
 * [com.bullseyestracker.cv.BoardCalibrationResult.NotFound] otherwise).
 *
 * Mirrors the geometry the DeepDarts reference implementation
 * (github.com/wmcnally/deep-darts, `dataset/annotate.py`) uses to go from 4 calibration points
 * to board geometry:
 * - Center is the centroid of the 4 points; radius is their mean distance from that centroid
 *   (`annotate.py`'s `get_circle`, commented `# double radius` -- the calibration points sit on
 *   the outer double-ring edge).
 * - Each calibration-point class has a fixed canonical angle on a zero-rotation board, 90
 *   degrees apart, derived from `annotate.py`'s default `transform(angle=9)` template and
 *   confirmed empirically against the training dataset's own labels (research.md §3): cal_1 =
 *   -9 deg, cal_4 = 81 deg, cal_2 = 171 deg, cal_3 = 261 deg (0 = top, clockwise, matching
 *   [com.bullseyestracker.cv.ScoreMapper]'s convention). The board's `rotationOffsetDegrees` is
 *   the (circular-mean) difference between each point's measured angle and its canonical angle.
 *
 * Ring ratios are the same regulation-board constants (BDO standard) as
 * [com.bullseyestracker.cv.opencv.OpenCvBoardDetector] uses for the classical backend.
 */
object CalibrationPointMapper {
    private val canonicalAngleDegrees =
        mapOf(
            1 to -9f,
            2 to 171f,
            3 to 261f,
            4 to 81f,
        )

    fun toBoardCalibration(calibrationPoints: Map<Int, Pair<Float, Float>>): BoardCalibration {
        require(calibrationPoints.keys == setOf(1, 2, 3, 4)) {
            "requires exactly one detection each for calibration-point classes 1..4, got ${calibrationPoints.keys}"
        }

        val centerX =
            calibrationPoints.values
                .map { it.first }
                .average()
                .toFloat()
        val centerY =
            calibrationPoints.values
                .map { it.second }
                .average()
                .toFloat()

        val outerRadius =
            calibrationPoints.values
                .map { (x, y) -> hypot((x - centerX).toDouble(), (y - centerY).toDouble()) }
                .average()
                .toFloat()

        val rotationOffsetDegrees = circularMeanRotationOffset(calibrationPoints, centerX, centerY)

        return BoardCalibration(
            centerX = centerX,
            centerY = centerY,
            innerBullRadius = outerRadius * INNER_BULL_RATIO,
            outerBullRadius = outerRadius * OUTER_BULL_RATIO,
            tripleRingInnerRadius = outerRadius * TRIPLE_INNER_RATIO,
            tripleRingOuterRadius = outerRadius * TRIPLE_OUTER_RATIO,
            doubleRingInnerRadius = outerRadius * DOUBLE_INNER_RATIO,
            doubleRingOuterRadius = outerRadius,
            rotationOffsetDegrees = rotationOffsetDegrees,
        )
    }

    /**
     * Circular mean of each point's (measured angle - canonical angle), since offsets near the
     * 0/360 wraparound must not cancel out in a plain arithmetic average (e.g. 359 and 1 should
     * average to 0, not 180).
     */
    private fun circularMeanRotationOffset(
        calibrationPoints: Map<Int, Pair<Float, Float>>,
        centerX: Float,
        centerY: Float,
    ): Float {
        var sumSin = 0.0
        var sumCos = 0.0
        for ((classId, point) in calibrationPoints) {
            val (x, y) = point
            val dx = (x - centerX).toDouble()
            val dy = (y - centerY).toDouble()
            val measuredAngle = Math.toDegrees(atan2(dx, -dy))
            val offset = measuredAngle - canonicalAngleDegrees.getValue(classId)
            sumSin += sin(Math.toRadians(offset))
            sumCos += cos(Math.toRadians(offset))
        }
        return Math.toDegrees(atan2(sumSin, sumCos)).toFloat()
    }

    // Regulation dartboard ring radii, relative to the outer double-ring edge (170mm) -- same
    // values as OpenCvBoardDetector's companion constants (BDO standard); duplicated rather than
    // shared to avoid coupling the DNN package to the classical detector's internals.
    private const val INNER_BULL_RATIO = 6.35f / 170f
    private const val OUTER_BULL_RATIO = 15.9f / 170f
    private const val TRIPLE_INNER_RATIO = 99f / 170f
    private const val TRIPLE_OUTER_RATIO = 107f / 170f
    private const val DOUBLE_INNER_RATIO = 162f / 170f
}
