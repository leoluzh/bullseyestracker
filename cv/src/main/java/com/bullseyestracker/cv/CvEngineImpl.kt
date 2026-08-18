package com.bullseyestracker.cv

/**
 * [dnnBoardDetector]/[dnnDartDetector] are nullable because the DNN backend is optional — the
 * bundled model asset can fail to load (spec 014-dnn-dart-detection Edge Cases), in which case
 * the caller passes `null` and this class transparently behaves as [DetectionBackend.CLASSICAL]
 * even while [detectionBackend] is set to [DetectionBackend.DNN] (contracts/dnn-detection-backend-contract.md).
 */
class CvEngineImpl(
    private val classicalBoardDetector: BoardDetector,
    private val classicalDartDetector: DartDetector,
    private val scoreMapper: ScoreMapper,
    private val dnnBoardDetector: BoardDetector? = null,
    private val dnnDartDetector: DartDetector? = null,
    override var detectionBackend: DetectionBackend = DetectionBackend.CLASSICAL,
) : CvEngine {
    private val boardDetector: BoardDetector
        get() = if (detectionBackend == DetectionBackend.DNN) dnnBoardDetector ?: classicalBoardDetector else classicalBoardDetector

    private val dartDetector: DartDetector
        get() = if (detectionBackend == DetectionBackend.DNN) dnnDartDetector ?: classicalDartDetector else classicalDartDetector

    override fun calibrateBoard(frame: FrameInput): BoardCalibrationResult = boardDetector.calibrate(frame)

    override fun detectThrows(
        frame: FrameInput,
        calibration: BoardCalibration,
    ): List<DetectedThrow> =
        dartDetector.detect(frame, calibration).map { raw ->
            val scored = scoreMapper.map(raw.positionX, raw.positionY, calibration)
            DetectedThrow(
                sectorNumber = scored.sectorNumber,
                ring = scored.ring,
                value = scored.value,
                confidence = raw.confidence,
                boardPositionX = raw.positionX,
                boardPositionY = raw.positionY,
            )
        }
}
