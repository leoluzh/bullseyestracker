package com.bullseyestracker.cv.opencv.dnn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Plain-JVM unit tests for [YoloV8OutputDecoder] — no OpenCV native runtime involved (data-model.md,
 * contracts/dnn-detection-backend-contract.md). Written test-first per constitution Principle III,
 * since this decoder determines which raw pixels become calibration points vs. dart tips before
 * any score is derived.
 *
 * Fixed synthetic tensors, laid out as `(4 + numClasses, numAnchors)` flattened row-major to
 * match [YoloV8RawOutput] (research.md §1: box `cx,cy,w,h` in model-input pixel space, followed
 * by per-class confidence rows).
 */
class YoloV8OutputDecoderTest {
    private val identityLetterbox =
        LetterboxInfo(scale = 1f, padX = 0f, padY = 0f, originalWidth = 640, originalHeight = 640)

    /** Builds a raw tensor from a list of (cx, cy, w, h, scoresPerClass) anchors. */
    private fun buildRawOutput(
        numClasses: Int,
        anchors: List<Anchor>,
    ): YoloV8RawOutput {
        val numAnchors = anchors.size
        val data = FloatArray((4 + numClasses) * numAnchors)
        anchors.forEachIndexed { i, anchor ->
            data[0 * numAnchors + i] = anchor.cx
            data[1 * numAnchors + i] = anchor.cy
            data[2 * numAnchors + i] = anchor.w
            data[3 * numAnchors + i] = anchor.h
            anchor.scores.forEachIndexed { c, score ->
                data[(4 + c) * numAnchors + i] = score
            }
        }
        return YoloV8RawOutput(data, numClasses, numAnchors)
    }

    private data class Anchor(
        val cx: Float,
        val cy: Float,
        val w: Float,
        val h: Float,
        val scores: List<Float>,
    )

    @Test
    fun `keeps a single detection above the confidence threshold`() {
        val raw =
            buildRawOutput(
                numClasses = 2,
                anchors = listOf(Anchor(cx = 320f, cy = 320f, w = 20f, h = 20f, scores = listOf(0.9f, 0.1f))),
            )

        val result = YoloV8OutputDecoder.decode(raw, identityLetterbox, confidenceThreshold = 0.5f, iouThreshold = 0.5f)

        assertEquals(1, result.size)
        assertEquals(0, result[0].classId)
        assertEquals(0.9f, result[0].confidence)
        assertEquals(320f / 640f, result[0].centerX, 1e-4f)
        assertEquals(320f / 640f, result[0].centerY, 1e-4f)
    }

    @Test
    fun `drops detections below the confidence threshold`() {
        val raw =
            buildRawOutput(
                numClasses = 1,
                anchors = listOf(Anchor(cx = 100f, cy = 100f, w = 10f, h = 10f, scores = listOf(0.2f))),
            )

        val result = YoloV8OutputDecoder.decode(raw, identityLetterbox, confidenceThreshold = 0.5f, iouThreshold = 0.5f)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `suppresses lower-confidence overlapping detections of the same class`() {
        val raw =
            buildRawOutput(
                numClasses = 1,
                anchors =
                    listOf(
                        Anchor(cx = 300f, cy = 300f, w = 40f, h = 40f, scores = listOf(0.95f)),
                        // Heavily overlapping box, same class, lower confidence -> suppressed.
                        Anchor(cx = 305f, cy = 305f, w = 40f, h = 40f, scores = listOf(0.7f)),
                    ),
            )

        val result = YoloV8OutputDecoder.decode(raw, identityLetterbox, confidenceThreshold = 0.5f, iouThreshold = 0.5f)

        assertEquals(1, result.size)
        assertEquals(0.95f, result[0].confidence)
    }

    @Test
    fun `does not suppress overlapping detections of different classes`() {
        val raw =
            buildRawOutput(
                numClasses = 2,
                anchors =
                    listOf(
                        Anchor(cx = 300f, cy = 300f, w = 40f, h = 40f, scores = listOf(0.9f, 0f)),
                        // Same box location, different class -> per-class NMS must keep both
                        // (calibration points and dart tips must not suppress each other).
                        Anchor(cx = 300f, cy = 300f, w = 40f, h = 40f, scores = listOf(0f, 0.85f)),
                    ),
            )

        val result = YoloV8OutputDecoder.decode(raw, identityLetterbox, confidenceThreshold = 0.5f, iouThreshold = 0.5f)

        assertEquals(2, result.size)
        assertEquals(setOf(0, 1), result.map { it.classId }.toSet())
    }

    @Test
    fun `keeps non-overlapping detections of the same class`() {
        val raw =
            buildRawOutput(
                numClasses = 1,
                anchors =
                    listOf(
                        Anchor(cx = 100f, cy = 100f, w = 20f, h = 20f, scores = listOf(0.9f)),
                        Anchor(cx = 500f, cy = 500f, w = 20f, h = 20f, scores = listOf(0.85f)),
                    ),
            )

        val result = YoloV8OutputDecoder.decode(raw, identityLetterbox, confidenceThreshold = 0.5f, iouThreshold = 0.5f)

        assertEquals(2, result.size)
    }

    @Test
    fun `un-letterboxes coordinates back into original-frame normalized space`() {
        // A 1280x640 original frame letterboxed into a 640x640 model input: scaled by 0.5,
        // padded 160px top/bottom (vertical padding), no horizontal padding.
        val letterbox = LetterboxInfo(scale = 0.5f, padX = 0f, padY = 160f, originalWidth = 1280, originalHeight = 640)
        val raw =
            buildRawOutput(
                numClasses = 1,
                // Model-space point (320, 320) -> original-space pixel ((320-0)/0.5, (320-160)/0.5) = (640, 320)
                anchors = listOf(Anchor(cx = 320f, cy = 320f, w = 10f, h = 10f, scores = listOf(0.9f))),
            )

        val result = YoloV8OutputDecoder.decode(raw, letterbox, confidenceThreshold = 0.5f, iouThreshold = 0.5f)

        assertEquals(1, result.size)
        assertEquals(640f / 1280f, result[0].centerX, 1e-4f)
        assertEquals(320f / 640f, result[0].centerY, 1e-4f)
    }
}
