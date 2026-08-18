package com.bullseyestracker.cv.opencv.dnn

import kotlin.math.max
import kotlin.math.min

/**
 * Turns a raw YOLOv8 ONNX detection-head output ([YoloV8RawOutput]) into usable detections:
 * per-anchor confidence thresholding (argmax over class scores), then per-class non-max
 * suppression so calibration-point and dart-tip detections never suppress each other
 * (contracts/dnn-detection-backend-contract.md). Pure function, no OpenCV/Android APIs, runs on
 * a plain JVM (data-model.md).
 */
object YoloV8OutputDecoder {
    fun decode(
        rawOutput: YoloV8RawOutput,
        letterbox: LetterboxInfo,
        confidenceThreshold: Float,
        iouThreshold: Float,
    ): List<YoloV8Detection> {
        val candidates = thresholdCandidates(rawOutput, confidenceThreshold)
        val kept = nonMaxSuppress(candidates, rawOutput.numClasses, iouThreshold)
        return kept.map { it.toDetection(letterbox) }
    }

    private fun thresholdCandidates(
        rawOutput: YoloV8RawOutput,
        confidenceThreshold: Float,
    ): List<Candidate> {
        val numAnchors = rawOutput.numAnchors
        val data = rawOutput.data
        val candidates = mutableListOf<Candidate>()

        for (anchor in 0 until numAnchors) {
            val cx = data[0 * numAnchors + anchor]
            val cy = data[1 * numAnchors + anchor]
            val w = data[2 * numAnchors + anchor]
            val h = data[3 * numAnchors + anchor]

            var bestClass = -1
            var bestScore = 0f
            for (c in 0 until rawOutput.numClasses) {
                val score = data[(4 + c) * numAnchors + anchor]
                if (score > bestScore) {
                    bestScore = score
                    bestClass = c
                }
            }

            if (bestClass >= 0 && bestScore >= confidenceThreshold) {
                candidates += Candidate(bestClass, cx, cy, w, h, bestScore)
            }
        }
        return candidates
    }

    private fun nonMaxSuppress(
        candidates: List<Candidate>,
        numClasses: Int,
        iouThreshold: Float,
    ): List<Candidate> {
        val kept = mutableListOf<Candidate>()
        for (classId in 0 until numClasses) {
            val remaining = candidates.filter { it.classId == classId }.sortedByDescending { it.confidence }.toMutableList()
            while (remaining.isNotEmpty()) {
                val best = remaining.removeAt(0)
                kept += best
                remaining.removeAll { iou(best, it) > iouThreshold }
            }
        }
        return kept
    }

    private fun iou(
        a: Candidate,
        b: Candidate,
    ): Float {
        val ax1 = a.cx - a.w / 2f
        val ay1 = a.cy - a.h / 2f
        val ax2 = a.cx + a.w / 2f
        val ay2 = a.cy + a.h / 2f
        val bx1 = b.cx - b.w / 2f
        val by1 = b.cy - b.h / 2f
        val bx2 = b.cx + b.w / 2f
        val by2 = b.cy + b.h / 2f

        val interX1 = max(ax1, bx1)
        val interY1 = max(ay1, by1)
        val interX2 = min(ax2, bx2)
        val interY2 = min(ay2, by2)
        val interWidth = max(0f, interX2 - interX1)
        val interHeight = max(0f, interY2 - interY1)
        val interArea = interWidth * interHeight

        val areaA = a.w * a.h
        val areaB = b.w * b.h
        val union = areaA + areaB - interArea
        return if (union <= 0f) 0f else interArea / union
    }

    private data class Candidate(
        val classId: Int,
        val cx: Float,
        val cy: Float,
        val w: Float,
        val h: Float,
        val confidence: Float,
    ) {
        fun toDetection(letterbox: LetterboxInfo): YoloV8Detection {
            val originalXPx = (cx - letterbox.padX) / letterbox.scale
            val originalYPx = (cy - letterbox.padY) / letterbox.scale
            return YoloV8Detection(
                classId = classId,
                centerX = (originalXPx / letterbox.originalWidth).coerceIn(0f, 1f),
                centerY = (originalYPx / letterbox.originalHeight).coerceIn(0f, 1f),
                confidence = confidence,
            )
        }
    }
}
