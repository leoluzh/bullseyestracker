package com.bullseyestracker.cv.opencv.dnn

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import kotlin.math.min

/**
 * Owns the loaded YOLOv8 ONNX net (`cv/src/main/assets/models/deepdarts-yolov8.onnx`,
 * NOTICE.md) and runs preprocessing (letterbox) + `Net.forward()` + [YoloV8OutputDecoder] for a
 * single frame. Shared by [OpenCvDnnBoardDetector] and [OpenCvDnnDartDetector] (both read the
 * same 5-class model, just filtering different class IDs) so the model is loaded into memory
 * once, not twice.
 *
 * Construction throws if the model bytes fail to load (`Net.empty()`) -- callers (`AppContainer`)
 * catch this and fall back to `DetectionBackend.CLASSICAL`-only operation (spec
 * 014-dnn-dart-detection Edge Cases; contracts/dnn-detection-backend-contract.md), rather than
 * this class silently degrading.
 */
class YoloV8Model(
    modelBytes: ByteArray,
) {
    private val net: Net = Dnn.readNetFromONNX(MatOfByte(*modelBytes))

    init {
        check(!net.empty()) { "YOLOv8 ONNX model failed to load (empty net)" }
    }

    fun detect(bitmap: Bitmap): List<YoloV8Detection> {
        val rgba = Mat()
        Utils.bitmapToMat(bitmap, rgba)
        val rgb = Mat()
        Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB)

        val letterboxed = Mat()
        val letterbox = letterbox(rgb, letterboxed)

        val blob =
            Dnn.blobFromImage(
                letterboxed,
                1.0 / 255.0,
                Size(MODEL_INPUT_SIZE.toDouble(), MODEL_INPUT_SIZE.toDouble()),
                Scalar(0.0, 0.0, 0.0),
                false,
                false,
            )
        net.setInput(blob)
        val output = net.forward()

        val totalRows = 4 + NUM_CLASSES
        val reshaped = output.reshape(1, totalRows)
        val numAnchors = reshaped.cols()
        val flat = FloatArray(totalRows * numAnchors)
        reshaped.get(0, 0, flat)

        rgba.release()
        rgb.release()
        letterboxed.release()
        blob.release()
        output.release()

        val rawOutput = YoloV8RawOutput(flat, NUM_CLASSES, numAnchors)
        return YoloV8OutputDecoder.decode(rawOutput, letterbox, CONFIDENCE_THRESHOLD, IOU_THRESHOLD)
    }

    /** Standard YOLO letterbox (research.md §2): resize preserving aspect ratio, pad with gray
     * (114,114,114) to a square [MODEL_INPUT_SIZE] input. */
    private fun letterbox(
        src: Mat,
        dst: Mat,
    ): LetterboxInfo {
        val originalWidth = src.cols()
        val originalHeight = src.rows()
        val scale = min(MODEL_INPUT_SIZE.toFloat() / originalWidth, MODEL_INPUT_SIZE.toFloat() / originalHeight)
        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()

        val resized = Mat()
        Imgproc.resize(src, resized, Size(newWidth.toDouble(), newHeight.toDouble()))

        val padX = (MODEL_INPUT_SIZE - newWidth) / 2
        val padY = (MODEL_INPUT_SIZE - newHeight) / 2
        val padRight = MODEL_INPUT_SIZE - newWidth - padX
        val padBottom = MODEL_INPUT_SIZE - newHeight - padY

        Core.copyMakeBorder(resized, dst, padY, padBottom, padX, padRight, Core.BORDER_CONSTANT, Scalar(114.0, 114.0, 114.0))
        resized.release()

        return LetterboxInfo(
            scale = scale,
            padX = padX.toFloat(),
            padY = padY.toFloat(),
            originalWidth = originalWidth,
            originalHeight = originalHeight,
        )
    }

    private companion object {
        const val MODEL_INPUT_SIZE = 640
        const val NUM_CLASSES = 5
        const val CONFIDENCE_THRESHOLD = 0.25f
        const val IOU_THRESHOLD = 0.45f
    }
}
