package com.bullseyestracker.cv

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * User Story 2: a single captured photo must score identically to the equivalent live frame —
 * CvEngine has no photo-vs-live mode of its own, just FrameInput in, DetectedThrow list out.
 * Uses the same fakes as CvEngineContractTest so this runs on a plain JVM (no OpenCV).
 */
class PhotoDetectionTest {
    private class FakeBoardDetector(private val result: BoardCalibrationResult) : BoardDetector {
        override fun calibrate(frame: FrameInput): BoardCalibrationResult = result
    }

    private class FakeDartDetector(private val detections: List<RawDartDetection>) : DartDetector {
        override fun detect(
            frame: FrameInput,
            calibration: BoardCalibration,
        ): List<RawDartDetection> = detections
    }

    private val calibration =
        BoardCalibration(
            centerX = 0.5f,
            centerY = 0.5f,
            innerBullRadius = 0.02f,
            outerBullRadius = 0.05f,
            tripleRingInnerRadius = 0.3f,
            tripleRingOuterRadius = 0.33f,
            doubleRingInnerRadius = 0.42f,
            doubleRingOuterRadius = 0.45f,
            rotationOffsetDegrees = 0f,
        )

    private val rawDetections =
        listOf(
            RawDartDetection(positionX = 0.5f, positionY = 0.2f, confidence = 0.9f),
            RawDartDetection(positionX = 0.6f, positionY = 0.55f, confidence = 0.8f),
            RawDartDetection(positionX = 0.5f, positionY = 0.5f, confidence = 0.95f),
        )

    private fun engine() =
        CvEngineImpl(
            boardDetector = FakeBoardDetector(BoardCalibrationResult.Calibrated(calibration, confidence = 0.9f)),
            dartDetector = FakeDartDetector(rawDetections),
            scoreMapper = ScoreMapper(),
        )

    @Test
    fun `a single photo capture scores identically to the same content as a live frame`() {
        // Same pixel content, same rotation — a photo (single takePicture() call) fed through
        // FrameInput must not be treated any differently than a frame pulled from the live
        // ImageAnalysis stream (LiveDetectionAnalyzer).
        val liveFrame = FrameInput(bitmap = mock(Bitmap::class.java), rotationDegrees = 90)
        val photoFrame = FrameInput(bitmap = mock(Bitmap::class.java), rotationDegrees = 90)

        val liveResult = engine().detectThrows(liveFrame, calibration)
        val photoResult = engine().detectThrows(photoFrame, calibration)

        assertEquals(liveResult, photoResult)
    }

    @Test
    fun `a photo frame scores every dart present, not just the first`() {
        val result = engine().detectThrows(FrameInput(bitmap = mock(Bitmap::class.java)), calibration)

        assertEquals(rawDetections.size, result.size)
    }

    @Test
    fun `repeated detectThrows calls on identical photo input are stable across invocations`() {
        // A live analyzer calls detectThrows() repeatedly as frames stream in; a photo screen
        // calls it exactly once. Detection must be a pure function of (frame, calibration) with
        // no hidden state that would make a single-shot photo call behave differently.
        val single = engine()
        val frame = FrameInput(bitmap = mock(Bitmap::class.java))

        val first = single.detectThrows(frame, calibration)
        val second = single.detectThrows(frame, calibration)
        val third = single.detectThrows(frame, calibration)

        assertEquals(first, second)
        assertEquals(second, third)
    }
}
