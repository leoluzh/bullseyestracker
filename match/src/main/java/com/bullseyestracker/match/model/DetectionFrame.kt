package com.bullseyestracker.match.model

enum class CaptureMode { PHOTO, LIVE_CAMERA }

data class DetectionFrame(
    val id: String,
    val captureMode: CaptureMode,
    /** Local reference (e.g. file path or content URI) to the stored/cached image data. */
    val imageRef: String,
    val capturedAt: Long,
    val boardDetected: Boolean
)
