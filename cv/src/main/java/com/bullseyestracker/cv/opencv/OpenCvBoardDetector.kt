package com.bullseyestracker.cv.opencv

import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.BoardCalibrationResult
import com.bullseyestracker.cv.BoardDetector
import com.bullseyestracker.cv.FrameInput
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * Locates the dartboard via Hough-circle detection on its outer printed edge, then derives the
 * rest of the scoring geometry from fixed regulation-board ring ratios (research.md board
 * localization decision). Requires the OpenCV native library to be loaded (`OpenCVLoader`)
 * before use — this class assumes that has already happened at app/process start.
 *
 * Radii are normalized against frame width only, which is a simplification: it assumes a
 * roughly square capture region around the board. A strongly non-square frame would need
 * separate x/y scaling — acceptable for v1 given CameraX's default capture aspect ratios.
 */
class OpenCvBoardDetector : BoardDetector {
    override fun calibrate(frame: FrameInput): BoardCalibrationResult {
        val rgba = Mat()
        Utils.bitmapToMat(frame.bitmap, rgba)

        val gray = Mat()
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.medianBlur(gray, gray, 5)

        val circles = Mat()
        Imgproc.HoughCircles(
            gray,
            circles,
            Imgproc.HOUGH_GRADIENT,
            1.0,
            gray.rows() / 8.0,
            100.0,
            60.0,
            (gray.cols() * MIN_RADIUS_FRACTION).toInt(),
            (gray.cols() * MAX_RADIUS_FRACTION).toInt(),
        )

        if (circles.cols() == 0) {
            rgba.release()
            gray.release()
            circles.release()
            return BoardCalibrationResult.NotFound
        }

        // HoughCircles orders results by accumulator strength; take the strongest match.
        val circleData = FloatArray(3)
        circles.get(0, 0, circleData)
        val centerXPx = circleData[0]
        val centerYPx = circleData[1]
        val outerRadiusPx = circleData[2]

        val width = rgba.cols().toFloat()
        val height = rgba.rows().toFloat()

        rgba.release()
        gray.release()
        circles.release()

        val calibration =
            BoardCalibration(
                centerX = centerXPx / width,
                centerY = centerYPx / height,
                innerBullRadius = outerRadiusPx * INNER_BULL_RATIO / width,
                outerBullRadius = outerRadiusPx * OUTER_BULL_RATIO / width,
                tripleRingInnerRadius = outerRadiusPx * TRIPLE_INNER_RATIO / width,
                tripleRingOuterRadius = outerRadiusPx * TRIPLE_OUTER_RATIO / width,
                doubleRingInnerRadius = outerRadiusPx * DOUBLE_INNER_RATIO / width,
                doubleRingOuterRadius = outerRadiusPx / width,
                rotationOffsetDegrees = 0f,
            )

        // Heuristic placeholder confidence — a single, cleanly detected dominant circle scores
        // high. Real confidence calibration needs empirical validation against T044's fixture set.
        return BoardCalibrationResult.Calibrated(calibration, confidence = 0.7f)
    }

    private companion object {
        // Regulation dartboard ring radii in mm, relative to the outer double-ring edge (170mm).
        const val INNER_BULL_RATIO = 6.35f / 170f
        const val OUTER_BULL_RATIO = 15.9f / 170f
        const val TRIPLE_INNER_RATIO = 99f / 170f
        const val TRIPLE_OUTER_RATIO = 107f / 170f
        const val DOUBLE_INNER_RATIO = 162f / 170f

        const val MIN_RADIUS_FRACTION = 0.2
        const val MAX_RADIUS_FRACTION = 0.55
    }
}
