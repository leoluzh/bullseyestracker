package com.bullseyestracker.di

import android.content.Context
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.cv.CvEngineImpl
import com.bullseyestracker.cv.ScoreMapper
import com.bullseyestracker.cv.opencv.OpenCvBoardDetector
import com.bullseyestracker.cv.opencv.OpenCvDartDetector
import com.bullseyestracker.match.data.MatchRepository

/**
 * Manual DI container. A framework (Hilt/Koin) would be unjustified complexity for this app's
 * small, static dependency graph — plain lazily-constructed singletons are enough.
 */
class AppContainer(context: Context) {
    val cvEngine: CvEngine by lazy {
        CvEngineImpl(
            boardDetector = OpenCvBoardDetector(),
            dartDetector = OpenCvDartDetector(),
            scoreMapper = ScoreMapper(),
        )
    }

    val matchRepository: MatchRepository by lazy {
        MatchRepository.create(context)
    }
}
