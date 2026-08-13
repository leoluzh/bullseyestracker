package com.bullseyestracker.cv

/**
 * Locates the dartboard within a frame and derives its scoring geometry. Split out from
 * [CvEngine] so the orchestration in [CvEngineImpl] stays unit-testable without an OpenCV
 * native runtime — only the OpenCV-backed implementation (`opencv/OpenCvBoardDetector.kt`)
 * needs a device/emulator to run its tests.
 */
interface BoardDetector {
    fun calibrate(frame: FrameInput): BoardCalibrationResult
}
