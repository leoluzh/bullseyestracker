package com.bullseyestracker.cv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bullseyestracker.cv.opencv.OpenCvBoardDetector
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader

/**
 * Instrumented (not a plain-JVM unit test) because OpenCvBoardDetector needs OpenCV's native
 * library, which only loads on an Android device/emulator — this is why these tests live in
 * androidTest rather than test, unlike ScoreMapperTest/CvEngineContractTest.
 *
 * Requires fixture images under `cv/src/androidTest/assets/fixtures/`
 * (`board_calibrated.png`, `no_board.png`). Curating that fixture set is tracked by
 * tasks.md T044 and is NOT included by this implementation pass — these tests will fail with
 * a missing-asset error until fixtures are added.
 */
@RunWith(AndroidJUnit4::class)
class OpenCvBoardDetectorTest {
    private lateinit var detector: OpenCvBoardDetector

    @Before
    fun setUp() {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        detector = OpenCvBoardDetector()
    }

    @Test
    fun detectsBoardInClearFixtureImage() {
        val result = detector.calibrate(FrameInput(loadFixture("board_calibrated.png")))

        assertTrue(result is BoardCalibrationResult.Calibrated)
        val confidence = (result as BoardCalibrationResult.Calibrated).confidence
        assertTrue("confidence should be reasonably high on a clear board photo", confidence > 0.5f)
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
