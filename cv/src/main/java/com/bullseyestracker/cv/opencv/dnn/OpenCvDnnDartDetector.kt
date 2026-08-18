package com.bullseyestracker.cv.opencv.dnn

import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.DartDetector
import com.bullseyestracker.cv.FrameInput
import com.bullseyestracker.cv.RawDartDetection
import kotlin.math.hypot

/**
 * DNN equivalent of [com.bullseyestracker.cv.opencv.OpenCvDartDetector]: detects the dart-tip
 * class (0) via [model]. The classical detector distinguishes "newly appeared since last call"
 * darts by diffing raw pixels against a baseline frame; this detector has no such baseline to
 * diff, so it instead tracks the *positions* of dart tips it has already reported and, each
 * call, returns only tips that don't spatially match ([MATCH_DISTANCE]) anything already
 * reported -- same "newly appeared" contract ([resetBaseline] included for parity), different
 * mechanism (research.md).
 */
class OpenCvDnnDartDetector(
    private val model: YoloV8Model,
) : DartDetector {
    private var baselineEstablished = false
    private val previouslyReported = mutableListOf<Pair<Float, Float>>()

    override fun detect(
        frame: FrameInput,
        calibration: BoardCalibration,
    ): List<RawDartDetection> {
        val current = model.detect(frame.bitmap).filter { it.classId == DART_TIP_CLASS_ID }

        if (!baselineEstablished) {
            baselineEstablished = true
            previouslyReported += current.map { it.centerX to it.centerY }
            return emptyList()
        }

        val newDetections =
            current.filter { detection ->
                previouslyReported.none { (px, py) -> distance(detection.centerX, detection.centerY, px, py) < MATCH_DISTANCE }
            }
        previouslyReported += newDetections.map { it.centerX to it.centerY }

        return newDetections.map { RawDartDetection(positionX = it.centerX, positionY = it.centerY, confidence = it.confidence) }
    }

    fun resetBaseline() {
        baselineEstablished = false
        previouslyReported.clear()
    }

    private fun distance(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
    ): Float = hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()).toFloat()

    private companion object {
        const val DART_TIP_CLASS_ID = 0

        // Normalized 0..1 frame units -- two detections closer than this are treated as the
        // same physical dart (detector jitter/re-detection), not a newly-thrown one.
        const val MATCH_DISTANCE = 0.03f
    }
}
