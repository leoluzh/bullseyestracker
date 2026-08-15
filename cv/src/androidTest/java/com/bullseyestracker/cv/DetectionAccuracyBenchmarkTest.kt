package com.bullseyestracker.cv

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bullseyestracker.cv.fixtures.DartGroundTruth
import com.bullseyestracker.cv.fixtures.FixtureGroundTruthParser
import com.bullseyestracker.cv.fixtures.findOrphanedFixtures
import com.bullseyestracker.cv.opencv.OpenCvBoardDetector
import com.bullseyestracker.cv.opencv.OpenCvDartDetector
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader

private const val ACCURACY_THRESHOLD = 0.90f
private const val FIXTURES_DIR = "fixtures"

/**
 * Spec 006-fixture-benchmark FR-005/FR-006/FR-007: runs the real detection pipeline
 * (OpenCvBoardDetector + OpenCvDartDetector + ScoreMapper, via DetectedThrow) against every
 * curated fixture and asserts overall sector+ring+multiplier accuracy meets the 90% target from
 * spec 001-dart-scoring-match's SC-002.
 *
 * Requires fixture images/ground-truth under `cv/src/androidTest/assets/fixtures/` — see the
 * README there. This test fails with a clear message (not a crash or a misleading accuracy
 * number) until real fixture photos are added (tasks.md Scope note); that is the expected state
 * until someone with physical access to a dartboard supplies them.
 */
@RunWith(AndroidJUnit4::class)
class DetectionAccuracyBenchmarkTest {
    @Test
    fun detectionAccuracyMeetsNinetyPercentThreshold() {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        val assets = InstrumentationRegistry.getInstrumentation().context.assets

        val entries = assets.list(FIXTURES_DIR).orEmpty()
        val pngNames = entries.filter { it.endsWith(".png") && !it.endsWith("_baseline.png") }.map { it.removeSuffix(".png") }.toSet()
        val jsonNames = entries.filter { it.endsWith(".json") }.map { it.removeSuffix(".json") }.toSet()

        val orphans = findOrphanedFixtures(pngNames, jsonNames)
        if (orphans.isNotEmpty()) {
            fail(
                "Fixture set is incomplete under cv/src/androidTest/assets/fixtures/ — missing " +
                    "the .png or .json half of: ${orphans.joinToString(", ")}. See the README in " +
                    "that directory.",
            )
        }

        if (pngNames.isEmpty()) {
            fail(
                "No fixture photos found under cv/src/androidTest/assets/fixtures/ — this is " +
                    "expected until real dartboard photos are added (spec 006-fixture-benchmark " +
                    "Assumptions); see the README in that directory for how to add them.",
            )
        }

        var totalExpected = 0
        var totalMatched = 0
        val report = StringBuilder()

        for (fixtureName in pngNames.sorted()) {
            val groundTruth = FixtureGroundTruthParser.parse(fixtureName, readAssetText(assets, "$FIXTURES_DIR/$fixtureName.json"))

            val calibrationResult = OpenCvBoardDetector().calibrate(FrameInput(loadFixtureBitmap(assets, "$fixtureName.png")))
            val calibration = calibrationResult as? BoardCalibrationResult.Calibrated
            if (calibration == null) {
                // No board found (e.g. a "no_board" fixture) — no darts can be scored either
                // side; contributes nothing to the accuracy total.
                continue
            }

            val detected = detectDarts(assets, fixtureName, groundTruth.darts, calibration.calibration)
            val matched = matchCount(groundTruth.darts, detected)

            totalExpected += groundTruth.darts.size
            totalMatched += matched
            report.append("$fixtureName: $matched/${groundTruth.darts.size}; ")
        }

        val accuracy = if (totalExpected == 0) 1f else totalMatched.toFloat() / totalExpected
        assertTrue(
            "detection accuracy was ${"%.1f".format(accuracy * 100)}% ($totalMatched/$totalExpected " +
                "darts correct), must be >= 90% (spec 001-dart-scoring-match SC-002). Per-fixture: $report",
            accuracy >= ACCURACY_THRESHOLD,
        )
    }

    /**
     * OpenCvDartDetector only reports darts newly appeared since its baseline (research.md) — a
     * zero-dart fixture establishes its own baseline trivially, while a darts-bearing fixture
     * needs a same-setup `<name>_baseline.png` to diff against (data-model.md).
     */
    private fun detectDarts(
        assets: AssetManager,
        fixtureName: String,
        groundTruthDarts: List<DartGroundTruth>,
        calibration: BoardCalibration,
    ): List<DetectedThrow> {
        val dartDetector = OpenCvDartDetector()
        val scoreMapper = ScoreMapper()

        if (groundTruthDarts.isEmpty()) {
            dartDetector.detect(FrameInput(loadFixtureBitmap(assets, "$fixtureName.png")), calibration)
            return emptyList()
        }

        val baselineName = "${fixtureName}_baseline.png"
        if (!assets.list(FIXTURES_DIR).orEmpty().contains(baselineName)) {
            fail(
                "Fixture '$fixtureName' has ${groundTruthDarts.size} ground-truth dart(s) but is " +
                    "missing its same-setup baseline photo ($FIXTURES_DIR/$baselineName) — see the " +
                    "README in that directory.",
            )
        }

        dartDetector.detect(FrameInput(loadFixtureBitmap(assets, baselineName)), calibration) // establishes baseline
        val raw = dartDetector.detect(FrameInput(loadFixtureBitmap(assets, "$fixtureName.png")), calibration)
        return raw.map { detection ->
            val scored = scoreMapper.map(detection.positionX, detection.positionY, calibration)
            DetectedThrow(
                sectorNumber = scored.sectorNumber,
                ring = scored.ring,
                value = scored.value,
                confidence = detection.confidence,
                boardPositionX = detection.positionX,
                boardPositionY = detection.positionY,
            )
        }
    }

    /** Greedy, unordered match on (sectorNumber, ring) — no partial credit (research.md). */
    private fun matchCount(
        expected: List<DartGroundTruth>,
        detected: List<DetectedThrow>,
    ): Int {
        val remaining = detected.toMutableList()
        var matches = 0
        for (dart in expected) {
            val matchIndex = remaining.indexOfFirst { it.sectorNumber == dart.sectorNumber && it.ring == dart.ring }
            if (matchIndex >= 0) {
                remaining.removeAt(matchIndex)
                matches++
            }
        }
        return matches
    }

    private fun readAssetText(
        assets: AssetManager,
        path: String,
    ): String = assets.open(path).bufferedReader().use { it.readText() }

    private fun loadFixtureBitmap(
        assets: AssetManager,
        name: String,
    ): Bitmap = assets.open("$FIXTURES_DIR/$name").use { BitmapFactory.decodeStream(it) }
}
