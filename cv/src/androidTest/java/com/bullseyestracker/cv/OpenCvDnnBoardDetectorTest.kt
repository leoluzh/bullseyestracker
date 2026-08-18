package com.bullseyestracker.cv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bullseyestracker.cv.opencv.dnn.OpenCvDnnBoardDetector
import com.bullseyestracker.cv.opencv.dnn.YoloV8Model
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader

/**
 * Instrumented (needs OpenCV's native library + the bundled ONNX asset, same reasons as
 * [OpenCvBoardDetectorTest]). Mirrors that test's structure so the two backends are directly
 * comparable (spec 014-dnn-dart-detection SC-001).
 *
 * Requires the same fixture images as [OpenCvBoardDetectorTest]
 * (`cv/src/androidTest/assets/fixtures/`) plus the bundled model asset
 * (`cv/src/main/assets/models/deepdarts-yolov8.onnx`).
 */
@RunWith(AndroidJUnit4::class)
class OpenCvDnnBoardDetectorTest {
    private lateinit var detector: OpenCvDnnBoardDetector

    @Before
    fun setUp() {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val modelBytes = context.assets.open("models/deepdarts-yolov8.onnx").use { it.readBytes() }
        detector = OpenCvDnnBoardDetector(YoloV8Model(modelBytes))
    }

    @Test
    fun detectsBoardInClearFixtureImage() {
        val result = detector.calibrate(FrameInput(loadFixture("board_calibrated.png")))

        assertTrue(result is BoardCalibrationResult.Calibrated)
        val confidence = (result as BoardCalibrationResult.Calibrated).confidence
        assertTrue("confidence should be reasonably high on a clear board photo", confidence > 0.25f)
    }

    @Test
    fun reportsNotFoundWhenNoBoardInFrame() {
        val result = detector.calibrate(FrameInput(loadFixture("no_board.png")))

        assertTrue(result is BoardCalibrationResult.NotFound)
    }

    private fun loadFixture(name: String): Bitmap {
        val context = InstrumentationRegistry.getInstrumentation().context
        context.assets.open("fixtures/$name").use { stream ->
            return BitmapFactory.decodeStream(stream)
        }
    }
}
