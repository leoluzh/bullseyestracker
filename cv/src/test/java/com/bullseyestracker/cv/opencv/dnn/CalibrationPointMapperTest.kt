package com.bullseyestracker.cv.opencv.dnn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

/**
 * Plain-JVM unit tests for [CalibrationPointMapper], written test-first per constitution
 * Principle III — this is the one piece of new logic in spec 014-dnn-dart-detection that
 * materially determines scoring geometry (research.md §3).
 *
 * Canonical angles (-9, 81, 171, 261 degrees for cal_1..cal_4, [ScoreMapper]'s 0=top/clockwise
 * convention) come from the DeepDarts reference implementation's `annotate.py` calibration
 * template and were confirmed empirically against the training dataset's own labels.
 */
class CalibrationPointMapperTest {
    private fun canonicalPoint(
        classId: Int,
        canonicalAngleDegrees: Float,
        centerX: Float = 0.5f,
        centerY: Float = 0.5f,
        radius: Float = 0.4f,
        rotationOffsetDegrees: Float = 0f,
    ): Pair<Float, Float> {
        val angle = Math.toRadians((canonicalAngleDegrees + rotationOffsetDegrees).toDouble())
        // Inverse of ScoreMapper's atan2(dx, -dy) convention (0 = top, clockwise).
        val dx = radius * sin(angle)
        val dy = -radius * cos(angle)
        return (centerX + dx).toFloat() to (centerY + dy).toFloat()
    }

    @Test
    fun `derives center as the centroid of the 4 calibration points`() {
        val points =
            mapOf(
                1 to canonicalPoint(1, -9f, centerX = 0.6f, centerY = 0.4f),
                2 to canonicalPoint(2, 171f, centerX = 0.6f, centerY = 0.4f),
                3 to canonicalPoint(3, 261f, centerX = 0.6f, centerY = 0.4f),
                4 to canonicalPoint(4, 81f, centerX = 0.6f, centerY = 0.4f),
            )

        val calibration = CalibrationPointMapper.toBoardCalibration(points)

        assertEquals(0.6f, calibration.centerX, 1e-3f)
        assertEquals(0.4f, calibration.centerY, 1e-3f)
    }

    @Test
    fun `derives ring radii from the mean distance to the calibration points`() {
        val points =
            mapOf(
                1 to canonicalPoint(1, -9f, radius = 0.4f),
                2 to canonicalPoint(2, 171f, radius = 0.4f),
                3 to canonicalPoint(3, 261f, radius = 0.4f),
                4 to canonicalPoint(4, 81f, radius = 0.4f),
            )

        val calibration = CalibrationPointMapper.toBoardCalibration(points)

        assertEquals(0.4f, calibration.doubleRingOuterRadius, 1e-3f)
        // Regulation ratios, same constants as OpenCvBoardDetector.
        assertEquals(0.4f * 6.35f / 170f, calibration.innerBullRadius, 1e-4f)
        assertEquals(0.4f * 15.9f / 170f, calibration.outerBullRadius, 1e-4f)
        assertEquals(0.4f * 99f / 170f, calibration.tripleRingInnerRadius, 1e-4f)
        assertEquals(0.4f * 107f / 170f, calibration.tripleRingOuterRadius, 1e-4f)
        assertEquals(0.4f * 162f / 170f, calibration.doubleRingInnerRadius, 1e-4f)
    }

    @Test
    fun `zero rotation offset when calibration points are in their canonical positions`() {
        val points =
            mapOf(
                1 to canonicalPoint(1, -9f),
                2 to canonicalPoint(2, 171f),
                3 to canonicalPoint(3, 261f),
                4 to canonicalPoint(4, 81f),
            )

        val calibration = CalibrationPointMapper.toBoardCalibration(points)

        assertEquals(0f, calibration.rotationOffsetDegrees, 0.5f)
    }

    @Test
    fun `derives rotation offset when the board is rotated in the image`() {
        val points =
            mapOf(
                1 to canonicalPoint(1, -9f, rotationOffsetDegrees = 30f),
                2 to canonicalPoint(2, 171f, rotationOffsetDegrees = 30f),
                3 to canonicalPoint(3, 261f, rotationOffsetDegrees = 30f),
                4 to canonicalPoint(4, 81f, rotationOffsetDegrees = 30f),
            )

        val calibration = CalibrationPointMapper.toBoardCalibration(points)

        assertEquals(30f, calibration.rotationOffsetDegrees, 0.5f)
    }

    @Test
    fun `requires exactly one detection for each calibration-point class`() {
        val incomplete = mapOf(1 to (0.5f to 0.1f), 2 to (0.5f to 0.9f), 3 to (0.1f to 0.5f))

        assertThrows(IllegalArgumentException::class.java) {
            CalibrationPointMapper.toBoardCalibration(incomplete)
        }
    }
}
