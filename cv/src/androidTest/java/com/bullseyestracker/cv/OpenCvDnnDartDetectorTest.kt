package com.bullseyestracker.cv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bullseyestracker.cv.opencv.dnn.OpenCvDnnBoardDetector
import com.bullseyestracker.cv.opencv.dnn.OpenCvDnnDartDetector
import com.bullseyestracker.cv.opencv.dnn.YoloV8Model
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader

/**
 * Instrumented for the same reason as [OpenCvDartDetectorTest]. Requires the same
 * `cv/src/androidTest/assets/fixtures/{empty_board,one_dart,three_darts}.png` fixtures, plus the
 * bundled model asset (`cv/src/main/assets/models/deepdarts-yolov8.onnx`).
 */
@RunWith(AndroidJUnit4::class)
class OpenCvDnnDartDetectorTest {
    private lateinit var boardDetector: OpenCvDnnBoardDetector
    private lateinit var dartDetector: OpenCvDnnDartDetector

    @Before
    fun setUp() {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val modelBytes = context.assets.open("models/deepdarts-yolov8.onnx").use { it.readBytes() }
        val model = YoloV8Model(modelBytes)
        boardDetector = OpenCvDnnBoardDetector(model)
        dartDetector = OpenCvDnnDartDetector(model)
    }

    @Test
    fun firstCallEstablishesBaselineAndReportsNoDarts() {
        val calibration = calibrateFromFixture("empty_board.png")

        val result = dartDetector.detect(FrameInput(loadFixture("empty_board.png")), calibration)

        assertEquals(0, result.size)
    }

    @Test
    fun detectsOneNewlyAppearedDart() {
        val calibration = calibrateFromFixture("empty_board.png")
        dartDetector.detect(FrameInput(loadFixture("empty_board.png")), calibration) // establishes baseline

        val result = dartDetector.detect(FrameInput(loadFixture("one_dart.png")), calibration)

        assertEquals(1, result.size)
    }

    @Test
    fun detectsThreeDartsAgainstEmptyBoardBaseline() {
        val calibration = calibrateFromFixture("empty_board.png")
        dartDetector.detect(FrameInput(loadFixture("empty_board.png")), calibration) // establishes baseline

        val result = dartDetector.detect(FrameInput(loadFixture("three_darts.png")), calibration)

        assertEquals(3, result.size)
    }

    private fun calibrateFromFixture(name: String): BoardCalibration {
        val result = boardDetector.calibrate(FrameInput(loadFixture(name)))
        check(result is BoardCalibrationResult.Calibrated) { "Fixture $name must contain a detectable board" }
        return result.calibration
    }

    private fun loadFixture(name: String): Bitmap {
        val context = InstrumentationRegistry.getInstrumentation().context
        context.assets.open("fixtures/$name").use { stream ->
            return BitmapFactory.decodeStream(stream)
        }
    }
}
