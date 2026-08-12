package com.bullseyestracker.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.BoardCalibrationResult
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.cv.FrameInput
import java.io.ByteArrayOutputStream

/**
 * CameraX ImageAnalysis.Analyzer wrapping CvEngine. CameraController binds this on a dedicated
 * background executor, so this class never runs on the UI thread (constitution Principle IV).
 *
 * Auto-calibrates on the first frames (spec Scenario 1: pointing at the board is enough, no
 * explicit "calibrate" action needed), then switches to per-frame dart detection once a board
 * is found. [onBoardNotFound] is throttled by the caller if needed — this class reports every
 * failed attempt.
 */
class LiveDetectionAnalyzer(
    private val cvEngine: CvEngine,
    private val onCalibrated: (BoardCalibration, confidence: Float) -> Unit,
    private val onBoardNotFound: () -> Unit,
    private val onDetections: (List<DetectedThrow>) -> Unit
) : ImageAnalysis.Analyzer {

    @Volatile private var calibration: BoardCalibration? = null

    fun resetCalibration() {
        calibration = null
    }

    override fun analyze(image: ImageProxy) {
        val bitmap = image.toBitmapOrNull()
        val rotationDegrees = image.imageInfo.rotationDegrees
        image.close()
        if (bitmap == null) return

        val frame = FrameInput(bitmap, rotationDegrees)
        val activeCalibration = calibration

        if (activeCalibration == null) {
            when (val result = cvEngine.calibrateBoard(frame)) {
                is BoardCalibrationResult.Calibrated -> {
                    calibration = result.calibration
                    onCalibrated(result.calibration, result.confidence)
                }
                BoardCalibrationResult.NotFound -> onBoardNotFound()
            }
            return
        }

        val detections = cvEngine.detectThrows(frame, activeCalibration)
        if (detections.isNotEmpty()) {
            onDetections(detections)
        }
    }
}

/**
 * Converts a YUV_420_888 ImageProxy (CameraX's default analysis format) to a Bitmap via a
 * JPEG round-trip — simple and reliable, at some CPU cost; revisit with direct YUV->RGB if
 * profiling (tasks.md T025) shows this dominates the frame budget.
 */
private fun ImageProxy.toBitmapOrNull(): Bitmap? {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
    val bytes = out.toByteArray()
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

    val rotation = imageInfo.rotationDegrees
    if (rotation == 0) return bitmap
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
