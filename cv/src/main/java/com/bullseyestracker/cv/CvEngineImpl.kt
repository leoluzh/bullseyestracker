package com.bullseyestracker.cv

class CvEngineImpl(
    private val boardDetector: BoardDetector,
    private val dartDetector: DartDetector,
    private val scoreMapper: ScoreMapper,
) : CvEngine {
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
