package com.bullseyestracker.cv.opencv

import com.bullseyestracker.cv.DartDetector
import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.FrameInput
import com.bullseyestracker.cv.RawDartDetection
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * Detects newly-appeared darts by diffing each frame against a baseline captured on the first
 * call (or after [resetBaseline]). Each call therefore returns darts that appeared *since* the
 * baseline was set, not the full board state — callers accumulate results across calls to build
 * a turn's darts, and MUST call [resetBaseline] when starting a new turn/photo session so
 * already-scored darts aren't re-diffed away.
 *
 * Requires the OpenCV native library to be loaded before use, same as [OpenCvBoardDetector].
 */
class OpenCvDartDetector : DartDetector {

    private var baseline: Mat? = null

    override fun detect(frame: FrameInput, calibration: BoardCalibration): List<RawDartDetection> {
        val rgba = Mat()
        Utils.bitmapToMat(frame.bitmap, rgba)

        val gray = Mat()
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

        val currentBaseline = baseline
        if (currentBaseline == null) {
            baseline = gray
            rgba.release()
            return emptyList()
        }

        val diff = Mat()
        Core.absdiff(gray, currentBaseline, diff)
        Imgproc.threshold(diff, diff, DIFF_THRESHOLD, 255.0, Imgproc.THRESH_BINARY)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(diff, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val width = rgba.cols().toFloat()
        val height = rgba.rows().toFloat()
        val minArea = width * height * MIN_AREA_RATIO
        val maxArea = width * height * MAX_AREA_RATIO

        val detections = contours.mapNotNull { contour ->
            val area = Imgproc.contourArea(contour)
            if (area < minArea || area > maxArea) return@mapNotNull null
            val moments = Imgproc.moments(contour)
            if (moments.m00 == 0.0) return@mapNotNull null
            RawDartDetection(
                positionX = (moments.m10 / moments.m00).toFloat() / width,
                positionY = (moments.m01 / moments.m00).toFloat() / height,
                confidence = (area / maxArea).toFloat().coerceIn(0f, 1f)
            )
        }

        gray.release()
        rgba.release()
        diff.release()
        hierarchy.release()
        contours.forEach { it.release() }

        return detections
    }

    fun resetBaseline() {
        baseline?.release()
        baseline = null
    }

    private companion object {
        const val DIFF_THRESHOLD = 30.0
        const val MIN_AREA_RATIO = 0.0005
        const val MAX_AREA_RATIO = 0.02
    }
}
