package com.bullseyestracker.cv

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class ScoreMapperTest {

    private val calibration = BoardCalibration(
        centerX = 500f,
        centerY = 500f,
        innerBullRadius = 10f,
        outerBullRadius = 25f,
        tripleRingInnerRadius = 150f,
        tripleRingOuterRadius = 165f,
        doubleRingInnerRadius = 210f,
        doubleRingOuterRadius = 225f,
        rotationOffsetDegrees = 0f
    )

    private val mapper = ScoreMapper()

    /** angleDegrees: 0 = top (12 o'clock), increasing clockwise, matching ScoreMapper's convention. */
    private fun pointAt(angleDegrees: Float, radius: Float): Pair<Float, Float> {
        val rad = Math.toRadians(angleDegrees.toDouble())
        val x = calibration.centerX + radius * sin(rad).toFloat()
        val y = calibration.centerY - radius * cos(rad).toFloat()
        return x to y
    }

    @Test
    fun `inner bull scores 50 with no sector`() {
        val (x, y) = pointAt(angleDegrees = 0f, radius = 5f)
        val result = mapper.map(x, y, calibration)
        assertEquals(Ring.INNER_BULL, result.ring)
        assertEquals(null, result.sectorNumber)
        assertEquals(50, result.value)
    }

    @Test
    fun `outer bull scores 25 with no sector`() {
        val (x, y) = pointAt(angleDegrees = 0f, radius = 18f)
        val result = mapper.map(x, y, calibration)
        assertEquals(Ring.OUTER_BULL, result.ring)
        assertEquals(25, result.value)
    }

    @Test
    fun `single 20 at top of board`() {
        val (x, y) = pointAt(angleDegrees = 0f, radius = 180f)
        val result = mapper.map(x, y, calibration)
        assertEquals(Ring.SINGLE, result.ring)
        assertEquals(20, result.sectorNumber)
        assertEquals(20, result.value)
    }

    @Test
    fun `triple 20 doubles down to triple ring band`() {
        val (x, y) = pointAt(angleDegrees = 0f, radius = 157f)
        val result = mapper.map(x, y, calibration)
        assertEquals(Ring.TRIPLE, result.ring)
        assertEquals(20, result.sectorNumber)
        assertEquals(60, result.value)
    }

    @Test
    fun `double 20 at outer ring band`() {
        val (x, y) = pointAt(angleDegrees = 0f, radius = 217f)
        val result = mapper.map(x, y, calibration)
        assertEquals(Ring.DOUBLE, result.ring)
        assertEquals(20, result.sectorNumber)
        assertEquals(40, result.value)
    }

    @Test
    fun `beyond the double ring is a miss`() {
        val (x, y) = pointAt(angleDegrees = 0f, radius = 230f)
        val result = mapper.map(x, y, calibration)
        assertEquals(Ring.MISS, result.ring)
        assertEquals(0, result.value)
    }

    @Test
    fun `sector 6 is five wedges clockwise from 20`() {
        val (x, y) = pointAt(angleDegrees = 90f, radius = 180f)
        val result = mapper.map(x, y, calibration)
        assertEquals(6, result.sectorNumber)
        assertEquals(Ring.SINGLE, result.ring)
    }

    @Test
    fun `sector 3 is directly opposite 20`() {
        val (x, y) = pointAt(angleDegrees = 180f, radius = 180f)
        val result = mapper.map(x, y, calibration)
        assertEquals(3, result.sectorNumber)
    }

    @Test
    fun `rotation offset shifts the whole sector layout`() {
        val rotated = calibration.copy(rotationOffsetDegrees = 18f)
        val (x, y) = pointAt(angleDegrees = 18f, radius = 180f)
        val result = mapper.map(x, y, rotated)
        assertEquals(20, result.sectorNumber)
    }
}
