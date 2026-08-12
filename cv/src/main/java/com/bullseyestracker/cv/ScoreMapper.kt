package com.bullseyestracker.cv

import kotlin.math.atan2
import kotlin.math.hypot

data class ScoreResult(val sectorNumber: Int?, val ring: Ring, val value: Int)

/**
 * Maps a pixel position to a dartboard sector/ring given the board's calibration geometry.
 * Sector order is the standard WDF dartboard layout, clockwise from the top (20).
 */
class ScoreMapper {

    private val sectorOrderClockwiseFromTwenty = intArrayOf(
        20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5
    )
    private val sectorWidthDegrees = 360f / 20f

    fun map(positionX: Float, positionY: Float, calibration: BoardCalibration): ScoreResult {
        val dx = positionX - calibration.centerX
        val dy = positionY - calibration.centerY
        val radius = hypot(dx.toDouble(), dy.toDouble()).toFloat()

        val (sectorNumber, ring) = when {
            radius <= calibration.innerBullRadius -> null to Ring.INNER_BULL
            radius <= calibration.outerBullRadius -> null to Ring.OUTER_BULL
            radius > calibration.doubleRingOuterRadius -> null to Ring.MISS
            else -> {
                val sector = sectorAt(dx, dy, calibration.rotationOffsetDegrees)
                val ring = ringForRadius(radius, calibration)
                sector to ring
            }
        }

        return ScoreResult(sectorNumber, ring, ScoreCalculator.valueFor(sectorNumber, ring))
    }

    private fun ringForRadius(radius: Float, c: BoardCalibration): Ring = when {
        radius <= c.tripleRingInnerRadius -> Ring.SINGLE
        radius <= c.tripleRingOuterRadius -> Ring.TRIPLE
        radius <= c.doubleRingInnerRadius -> Ring.SINGLE
        else -> Ring.DOUBLE
    }

    private fun sectorAt(dx: Float, dy: Float, rotationOffsetDegrees: Float): Int {
        // atan2(dx, -dy): 0 degrees points to the top of the board (12 o'clock), increasing
        // clockwise, matching how sectorOrderClockwiseFromTwenty is laid out.
        val angleDegrees = Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat()
        val adjusted = (angleDegrees - rotationOffsetDegrees + sectorWidthDegrees / 2f + 360f) % 360f
        val index = (adjusted / sectorWidthDegrees).toInt().coerceIn(0, 19)
        return sectorOrderClockwiseFromTwenty[index]
    }
}
