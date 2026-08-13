package com.bullseyestracker.cv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bullseyestracker.cv.opencv.OpenCvBoardDetector
import com.bullseyestracker.cv.opencv.OpenCvDartDetector
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader

/**
 * Validates the <200ms frame-to-result budget (constitution Principle IV, spec.md Success
 * Criteria) on the reference device class pinned in research.md (Pixel 6a / Galaxy A54-class
 * or equivalent) — run this on a matching physical device or emulator profile, not a flagship,
 * or the result understates real-world latency. Requires the same fixtures as
 * OpenCvDartDetectorTest (tasks.md T044 gap).
 */
@RunWith(AndroidJUnit4::class)
class PerformanceBenchmarkTest {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var dartDetector: OpenCvDartDetector
    private lateinit var calibration: BoardCalibration

    @Before
    fun setUp() {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        val boardDetector = OpenCvBoardDetector()
        dartDetector = OpenCvDartDetector()

        val calibrated = boardDetector.calibrate(FrameInput(loadFixture("empty_board.png")))
        check(calibrated is BoardCalibrationResult.Calibrated) { "empty_board.png must contain a detectable board" }
        calibration = calibrated.calibration
        dartDetector.detect(FrameInput(loadFixture("empty_board.png")), calibration) // establishes baseline
    }

    @Test
    fun detectThrowsStaysUnderPerformanceBudget() {
        val frame = FrameInput(loadFixture("three_darts.png"))

        benchmarkRule.measureRepeated {
            dartDetector.detect(frame, calibration)
        }

        // measureRepeated reports median/percentile timings to the instrumentation log; also
        // assert a single-run hard budget so this test fails loudly on a regression, not just
        // silently in a benchmark report nobody reads.
        val start = System.nanoTime()
        dartDetector.detect(frame, calibration)
        val elapsedMillis = (System.nanoTime() - start) / 1_000_000
        assertTrue("frame-to-result must stay under 200ms, was ${elapsedMillis}ms", elapsedMillis < 200)
    }

    private fun loadFixture(name: String): Bitmap {
        val context = InstrumentationRegistry.getInstrumentation().context
        context.assets.open("fixtures/$name").use { stream ->
            return BitmapFactory.decodeStream(stream)
        }
    }
}
