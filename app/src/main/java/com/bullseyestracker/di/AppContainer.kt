package com.bullseyestracker.di

import android.content.Context
import android.util.Log
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.cv.CvEngineImpl
import com.bullseyestracker.cv.ScoreMapper
import com.bullseyestracker.cv.opencv.OpenCvBoardDetector
import com.bullseyestracker.cv.opencv.OpenCvDartDetector
import com.bullseyestracker.cv.opencv.dnn.OpenCvDnnBoardDetector
import com.bullseyestracker.cv.opencv.dnn.OpenCvDnnDartDetector
import com.bullseyestracker.cv.opencv.dnn.YoloV8Model
import com.bullseyestracker.match.data.MatchRepository
import com.bullseyestracker.ui.settings.DetectionBackendSetting

private const val TAG = "AppContainer"
private const val DNN_MODEL_ASSET_PATH = "models/deepdarts-yolov8.onnx"

/**
 * Manual DI container. A framework (Hilt/Koin) would be unjustified complexity for this app's
 * small, static dependency graph — plain lazily-constructed singletons are enough.
 */
class AppContainer(
    private val context: Context,
) {
    val detectionBackendSetting: DetectionBackendSetting by lazy { DetectionBackendSetting(context) }

    /**
     * `null` when the bundled DNN model asset failed to load (spec 014-dnn-dart-detection Edge
     * Cases) — [CvEngineImpl] falls back to classical-only behavior in that case even if the
     * user has [DetectionBackend.DNN][com.bullseyestracker.cv.DetectionBackend] selected.
     */
    private val dnnModel: YoloV8Model? by lazy {
        runCatching {
            val bytes = context.assets.open(DNN_MODEL_ASSET_PATH).use { it.readBytes() }
            YoloV8Model(bytes)
        }.onFailure { e ->
            Log.w(TAG, "DNN detection model failed to load — falling back to classical detection only", e)
        }.getOrNull()
    }

    /** Whether the DNN detection backend loaded successfully and can be selected (spec
     * 014-dnn-dart-detection FR-004/Edge Cases) -- read by the settings UI to disable the DNN
     * option rather than let the user pick a backend that silently falls back to classical. */
    val isDnnAvailable: Boolean get() = dnnModel != null

    val cvEngine: CvEngine by lazy {
        val model = dnnModel
        CvEngineImpl(
            classicalBoardDetector = OpenCvBoardDetector(),
            classicalDartDetector = OpenCvDartDetector(),
            scoreMapper = ScoreMapper(),
            dnnBoardDetector = model?.let { OpenCvDnnBoardDetector(it) },
            dnnDartDetector = model?.let { OpenCvDnnDartDetector(it) },
            detectionBackend = detectionBackendSetting.get(),
        )
    }

    val matchRepository: MatchRepository by lazy {
        MatchRepository.create(context)
    }
}
