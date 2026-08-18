package com.bullseyestracker.cv

import android.graphics.Bitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Verifies CvEngineImpl's orchestration contract (contracts/cv-engine-contract.md) using fake
 * BoardDetector/DartDetector — no OpenCV involved, so this runs on a plain JVM.
 */
class CvEngineContractTest {
    private class FakeBoardDetector(
        private val result: BoardCalibrationResult,
    ) : BoardDetector {
        override fun calibrate(frame: FrameInput): BoardCalibrationResult = result
    }

    private class FakeDartDetector(
        private val detections: List<RawDartDetection>,
    ) : DartDetector {
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

    // FrameInput.bitmap is never read by CvEngineImpl/ScoreMapper — a mock stands in so the
    // data class can be constructed without needing a real Android runtime.
    private fun fakeFrame() = FrameInput(bitmap = mock(Bitmap::class.java))

    @Test
    fun `detectThrows returns empty list, not an error, when no darts are found`() {
        val engine =
            CvEngineImpl(
                classicalBoardDetector = FakeBoardDetector(BoardCalibrationResult.NotFound),
                classicalDartDetector = FakeDartDetector(emptyList()),
                scoreMapper = ScoreMapper(),
            )

        val result = engine.detectThrows(fakeFrame(), calibration)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `every detected throw carries confidence and its normalized board position`() {
        val engine =
            CvEngineImpl(
                classicalBoardDetector = FakeBoardDetector(BoardCalibrationResult.Calibrated(calibration, confidence = 0.9f)),
                classicalDartDetector =
                    FakeDartDetector(
                        listOf(RawDartDetection(positionX = 0.5f, positionY = 0.2f, confidence = 0.75f)),
                    ),
                scoreMapper = ScoreMapper(),
            )

        val result = engine.detectThrows(fakeFrame(), calibration)

        assertEquals(1, result.size)
        val detected = result.first()
        assertEquals(0.75f, detected.confidence)
        assertEquals(0.5f, detected.boardPositionX)
        assertEquals(0.2f, detected.boardPositionY)
    }

    @Test
    fun `calibrateBoard delegates to the board detector`() {
        val engine =
            CvEngineImpl(
                classicalBoardDetector = FakeBoardDetector(BoardCalibrationResult.NotFound),
                classicalDartDetector = FakeDartDetector(emptyList()),
                scoreMapper = ScoreMapper(),
            )

        assertEquals(BoardCalibrationResult.NotFound, engine.calibrateBoard(fakeFrame()))
    }

    @Test
    fun `defaults to the classical backend`() {
        val engine =
            CvEngineImpl(
                classicalBoardDetector = FakeBoardDetector(BoardCalibrationResult.NotFound),
                classicalDartDetector = FakeDartDetector(emptyList()),
                scoreMapper = ScoreMapper(),
            )

        assertEquals(DetectionBackend.CLASSICAL, engine.detectionBackend)
    }

    @Test
    fun `switching to DNN takes effect on the next call without recreating the engine`() {
        val engine =
            CvEngineImpl(
                classicalBoardDetector = FakeBoardDetector(BoardCalibrationResult.NotFound),
                classicalDartDetector = FakeDartDetector(emptyList()),
                scoreMapper = ScoreMapper(),
                dnnBoardDetector = FakeBoardDetector(BoardCalibrationResult.Calibrated(calibration, confidence = 0.8f)),
                dnnDartDetector = FakeDartDetector(emptyList()),
            )

        assertEquals(BoardCalibrationResult.NotFound, engine.calibrateBoard(fakeFrame()))

        engine.detectionBackend = DetectionBackend.DNN

        assertEquals(
            BoardCalibrationResult.Calibrated(calibration, confidence = 0.8f),
            engine.calibrateBoard(fakeFrame()),
        )
    }

    @Test
    fun `DNN backend falls back to classical when no DNN detectors were supplied`() {
        val engine =
            CvEngineImpl(
                classicalBoardDetector = FakeBoardDetector(BoardCalibrationResult.Calibrated(calibration, confidence = 0.7f)),
                classicalDartDetector = FakeDartDetector(emptyList()),
                scoreMapper = ScoreMapper(),
                dnnBoardDetector = null,
                dnnDartDetector = null,
                detectionBackend = DetectionBackend.DNN,
            )

        assertEquals(
            BoardCalibrationResult.Calibrated(calibration, confidence = 0.7f),
            engine.calibrateBoard(fakeFrame()),
        )
    }
}
