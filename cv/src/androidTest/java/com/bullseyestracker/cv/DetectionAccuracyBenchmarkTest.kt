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
import com.bullseyestracker.cv.opencv.dnn.OpenCvDnnBoardDetector
import com.bullseyestracker.cv.opencv.dnn.OpenCvDnnDartDetector
import com.bullseyestracker.cv.opencv.dnn.YoloV8Model
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader

private const val ACCURACY_THRESHOLD = 0.90f
private const val FIXTURES_DIR = "fixtures"

/**
 * Spec 006-fixture-benchmark FR-005/FR-006/FR-007 (classical backend), extended by spec
 * 014-dnn-dart-detection SC-001 to also run the DNN backend on the same fixtures and assert its
 * accuracy is >= the classical backend's — both via the real detection pipeline
 * (BoardDetector + DartDetector + ScoreMapper, via DetectedThrow).
 *
 * Requires fixture images/ground-truth under `cv/src/androidTest/assets/fixtures/` — see the
 * README there — plus, for the DNN portion, the bundled model asset
 * (`cv/src/main/assets/models/deepdarts-yolov8.onnx`). Fails with a clear message (not a crash
 * or a misleading accuracy number) until real fixture photos are added (tasks.md Scope note);
 * that is the expected state until someone with physical access to a dartboard supplies them.
 */
@RunWith(AndroidJUnit4::class)
class DetectionAccuracyBenchmarkTest {
    @Test
    fun detectionAccuracyMeetsNinetyPercentThreshold() {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val pngNames = requireFixtureSet(assets)

        val accuracy = computeAccuracy(assets, pngNames, boardDetector = OpenCvBoardDetector()) { OpenCvDartDetector() }

        assertTrue(
            "classical detection accuracy was ${"%.1f".format(accuracy.percent)}% " +
                "(${accuracy.matched}/${accuracy.expected} darts correct), must be >= 90% " +
                "(spec 001-dart-scoring-match SC-002). Per-fixture: ${accuracy.report}",
            accuracy.percent >= ACCURACY_THRESHOLD * 100f,
        )
    }

    @Test
    fun dnnBackendAccuracyMeetsOrExceedsClassical() {
        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        val context = InstrumentationRegistry.getInstrumentation()
        val assets = context.context.assets
        val pngNames = requireFixtureSet(assets)

        val classical = computeAccuracy(assets, pngNames, boardDetector = OpenCvBoardDetector()) { OpenCvDartDetector() }

        val modelBytes =
            context.targetContext.assets
                .open("models/deepdarts-yolov8.onnx")
                .use { it.readBytes() }
        val model = YoloV8Model(modelBytes)
        val dnn = computeAccuracy(assets, pngNames, boardDetector = OpenCvDnnBoardDetector(model)) { OpenCvDnnDartDetector(model) }

        assertTrue(
            "DNN backend accuracy (${"%.1f".format(dnn.percent)}%, ${dnn.matched}/${dnn.expected}) must be " +
                ">= classical backend accuracy (${"%.1f".format(classical.percent)}%, " +
                "${classical.matched}/${classical.expected}) -- spec 014-dnn-dart-detection SC-001. " +
                "Per-fixture (DNN): ${dnn.report}",
            dnn.percent >= classical.percent,
        )
    }

    private fun requireFixtureSet(assets: AssetManager): Set<String> {
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
        return pngNames
    }

    private data class AccuracyResult(
        val expected: Int,
        val matched: Int,
        val report: String,
    ) {
        val percent: Float get() = if (expected == 0) 100f else matched.toFloat() / expected * 100f
    }

    private fun computeAccuracy(
        assets: AssetManager,
        pngNames: Set<String>,
        boardDetector: BoardDetector,
        dartDetectorFactory: () -> DartDetector,
    ): AccuracyResult {
        var totalExpected = 0
        var totalMatched = 0
        val report = StringBuilder()

        for (fixtureName in pngNames.sorted()) {
            val groundTruth = FixtureGroundTruthParser.parse(fixtureName, readAssetText(assets, "$FIXTURES_DIR/$fixtureName.json"))

            val calibrationResult = boardDetector.calibrate(FrameInput(loadFixtureBitmap(assets, "$fixtureName.png")))
            val calibration = calibrationResult as? BoardCalibrationResult.Calibrated
            if (calibration == null) {
                // No board found (e.g. a "no_board" fixture) — no darts can be scored either
                // side; contributes nothing to the accuracy total.
                continue
            }

            val detected = detectDarts(assets, fixtureName, groundTruth.darts, calibration.calibration, dartDetectorFactory())
            val matched = matchCount(groundTruth.darts, detected)

            totalExpected += groundTruth.darts.size
            totalMatched += matched
            report.append("$fixtureName: $matched/${groundTruth.darts.size}; ")
        }

        return AccuracyResult(totalExpected, totalMatched, report.toString())
    }

    /**
     * Both [OpenCvDartDetector] and [OpenCvDnnDartDetector] only report darts newly appeared
     * since a baseline call (research.md) — a zero-dart fixture establishes its own baseline
     * trivially, while a darts-bearing fixture needs a same-setup `<name>_baseline.png` to diff
     * against (data-model.md).
     */
    private fun detectDarts(
        assets: AssetManager,
        fixtureName: String,
        groundTruthDarts: List<DartGroundTruth>,
        calibration: BoardCalibration,
        dartDetector: DartDetector,
    ): List<DetectedThrow> {
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
