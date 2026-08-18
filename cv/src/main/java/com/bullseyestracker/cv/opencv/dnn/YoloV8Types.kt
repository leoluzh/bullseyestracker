package com.bullseyestracker.cv.opencv.dnn

/**
 * One decoded YOLOv8 detection, in the original frame's normalized 0..1 space (already
 * un-letterboxed — see [LetterboxInfo]). `classId` follows the DeepDarts dataset's layout:
 * 0 = dart tip, 1..4 = board calibration points (`cal_1`..`cal_4`).
 */
data class YoloV8Detection(
    val classId: Int,
    val centerX: Float,
    val centerY: Float,
    val confidence: Float,
)

/**
 * Records how a frame was resized/padded to fit the model's square input, so
 * [YoloV8OutputDecoder] can map model-space coordinates back to the original frame's normalized
 * space. `scale` is the factor the original image was multiplied by to fit inside the model
 * input (preserving aspect ratio); `padX`/`padY` are the letterbox padding, in model-input pixel
 * units, added to center the resized image.
 */
data class LetterboxInfo(
    val scale: Float,
    val padX: Float,
    val padY: Float,
    val originalWidth: Int,
    val originalHeight: Int,
)

/**
 * Raw YOLOv8 ONNX detection-head output before decoding — shape `(4 + numClasses, numAnchors)`,
 * flattened row-major: rows `0..3` are box `cx,cy,w,h` in model-input pixel space, rows
 * `4..4+numClasses-1` are per-class confidence scores. Matches the export this project's model
 * uses (`opset=12`, `nms=False` — see research.md §1), i.e. `Net.forward()`'s output reshaped to
 * 2D before being read into this flat array.
 */
data class YoloV8RawOutput(
    val data: FloatArray,
    val numClasses: Int,
    val numAnchors: Int,
)
