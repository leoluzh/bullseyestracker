package com.bullseyestracker.cv.opencv.dnn

import com.bullseyestracker.cv.BoardCalibrationResult
import com.bullseyestracker.cv.BoardDetector
import com.bullseyestracker.cv.FrameInput

/**
 * DNN equivalent of [com.bullseyestracker.cv.opencv.OpenCvBoardDetector]: detects the 4
 * DeepDarts calibration-point classes (1..4) via [model] and derives a `BoardCalibration` from
 * them using [CalibrationPointMapper] (research.md §3). Requires exactly one detection per
 * calibration-point class, same as the classical backend requires a clean Hough-circle match --
 * returns [BoardCalibrationResult.NotFound] otherwise (spec FR-006,
 * contracts/dnn-detection-backend-contract.md).
 */
class OpenCvDnnBoardDetector(
    private val model: YoloV8Model,
) : BoardDetector {
    override fun calibrate(frame: FrameInput): BoardCalibrationResult {
        val byClass = model.detect(frame.bitmap).filter { it.classId in 1..4 }.groupBy { it.classId }

        if (!(1..4).all { byClass[it]?.size == 1 }) {
            // Missing a class entirely, or more than one candidate survived NMS for a class that
            // should be unique on a real board -- treat as a failed detection rather than
            // guessing which candidate is correct (spec FR-006).
            return BoardCalibrationResult.NotFound
        }

        val points = byClass.mapValues { (_, detections) -> detections.single().let { it.centerX to it.centerY } }
        val calibration = CalibrationPointMapper.toBoardCalibration(points)
        val confidence = byClass.values.flatten().minOf { it.confidence }

        return BoardCalibrationResult.Calibrated(calibration, confidence)
    }
}
