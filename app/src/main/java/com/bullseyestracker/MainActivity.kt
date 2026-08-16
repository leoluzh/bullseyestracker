package com.bullseyestracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bullseyestracker.cv.CvNativeInit
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.di.AppContainer
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.ui.calibration.CalibrationTestScreen
import com.bullseyestracker.ui.detection.CaptureMode
import com.bullseyestracker.ui.detection.CaptureModeSelector
import com.bullseyestracker.ui.detection.LiveScoringScreen
import com.bullseyestracker.ui.detection.PhotoScoringScreen
import com.bullseyestracker.ui.history.HistoryViewModel
import com.bullseyestracker.ui.history.HistoryViewModelFactory
import com.bullseyestracker.ui.history.MatchHistoryDetailScreen
import com.bullseyestracker.ui.history.MatchHistoryScreen
import com.bullseyestracker.ui.home.GameModeListScreen
import com.bullseyestracker.ui.home.HomeScreen
import com.bullseyestracker.ui.home.SplashScreen
import com.bullseyestracker.ui.match.CricketScoreboardScreen
import com.bullseyestracker.ui.match.FiveOOneScoreboardScreen
import com.bullseyestracker.ui.match.MatchSetupScreen
import com.bullseyestracker.ui.match.MatchViewModel
import com.bullseyestracker.ui.match.MatchViewModelFactory
import com.bullseyestracker.ui.stats.PlayerStatsScreen
import com.bullseyestracker.ui.stats.PlayerStatsViewModel
import com.bullseyestracker.ui.stats.PlayerStatsViewModelFactory
import com.bullseyestracker.ui.theme.BullseyesTrackerTheme
import kotlinx.coroutines.delay

/** Fixed splash duration (spec 013-app-home-navigation FR-002) — no network/long-running work
 * to wait on since the app is fully offline (constitution Principle I). */
private const val SPLASH_DURATION_MS = 1200L

/**
 * App-wide navigation state (spec 005-match-history User Story 2, extended by spec
 * 010-player-stats and spec 013-app-home-navigation). [Splash] and [Home] are the only states
 * reachable regardless of an in-progress match (see the `screen == Splash` gate in `onCreate`);
 * every other state is only reachable when no match is in-progress — an in-progress/resumed
 * match always takes over the UI via [MatchViewModel.match] regardless of this state (spec
 * FR-001-003, unaffected by this feature).
 */
private sealed class AppScreen {
    data object Splash : AppScreen()

    data object Home : AppScreen()

    data object GameModeList : AppScreen()

    data class Setup(
        val gameMode: GameMode,
    ) : AppScreen()

    data object History : AppScreen()

    data class HistoryDetail(
        val matchId: String,
    ) : AppScreen()

    data object Stats : AppScreen()

    data object CalibrationTest : AppScreen()
}

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer
    private var cameraPermissionGranted by mutableStateOf(false)

    private val requestCameraPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> cameraPermissionGranted = granted }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        check(CvNativeInit.load()) { "OpenCV native library failed to load" }
        appContainer = AppContainer(applicationContext)

        cameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionGranted) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        setContent {
            var captureMode by remember { mutableStateOf(CaptureMode.LIVE_CAMERA) }
            var screen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
            val matchViewModel: MatchViewModel =
                viewModel(factory = MatchViewModelFactory(appContainer.matchRepository))
            val historyViewModel: HistoryViewModel =
                viewModel(factory = HistoryViewModelFactory(appContainer.matchRepository))
            val playerStatsViewModel: PlayerStatsViewModel =
                viewModel(factory = PlayerStatsViewModelFactory(appContainer.matchRepository))
            val match by matchViewModel.match.collectAsState()
            val currentMatch = match

            BullseyesTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (screen == AppScreen.Splash && currentMatch == null) {
                        SplashScreen()
                        LaunchedEffect(Unit) {
                            delay(SPLASH_DURATION_MS)
                            screen = AppScreen.Home
                        }
                    } else if (cameraPermissionGranted) {
                        if (currentMatch == null) {
                            when (val currentScreen = screen) {
                                AppScreen.Splash -> Unit // unreachable: gated above alongside currentMatch == null
                                AppScreen.Home ->
                                    HomeScreen(
                                        onNewGame = { screen = AppScreen.GameModeList },
                                        onMatchHistory = { screen = AppScreen.History },
                                        onPlayerStats = { screen = AppScreen.Stats },
                                        onTestCalibrator = { screen = AppScreen.CalibrationTest },
                                    )
                                AppScreen.GameModeList ->
                                    GameModeListScreen(
                                        onModeSelected = { mode -> screen = AppScreen.Setup(mode) },
                                        onBack = { screen = AppScreen.Home },
                                    )
                                is AppScreen.Setup ->
                                    MatchSetupScreen(
                                        gameMode = currentScreen.gameMode,
                                        onStartMatch = matchViewModel::startMatch,
                                        onBack = { screen = AppScreen.GameModeList },
                                    )
                                AppScreen.History ->
                                    MatchHistoryScreen(
                                        viewModel = historyViewModel,
                                        onMatchSelected = { matchId ->
                                            historyViewModel.selectMatch(matchId)
                                            screen = AppScreen.HistoryDetail(matchId)
                                        },
                                        onBack = { screen = AppScreen.Home },
                                    )
                                is AppScreen.HistoryDetail -> {
                                    val selectedMatch by historyViewModel.selectedMatch.collectAsState()
                                    selectedMatch?.let { detailMatch ->
                                        MatchHistoryDetailScreen(
                                            match = detailMatch,
                                            onBack = {
                                                historyViewModel.clearSelection()
                                                screen = AppScreen.History
                                            },
                                        )
                                    }
                                }
                                AppScreen.Stats ->
                                    PlayerStatsScreen(
                                        viewModel = playerStatsViewModel,
                                        onBack = { screen = AppScreen.Home },
                                    )
                                AppScreen.CalibrationTest ->
                                    CalibrationTestScreen(
                                        cvEngine = appContainer.cvEngine,
                                        onBack = { screen = AppScreen.Home },
                                    )
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                when (currentMatch.gameMode) {
                                    GameMode.FIVE_O_ONE -> FiveOOneScoreboardScreen(match = currentMatch)
                                    GameMode.CRICKET -> CricketScoreboardScreen(match = currentMatch)
                                }
                                if (currentMatch.status == MatchStatus.COMPLETED) {
                                    Button(onClick = { matchViewModel.startNewMatch() }) {
                                        Text("New match")
                                    }
                                } else {
                                    CaptureModeSelector(
                                        selected = captureMode,
                                        onSelected = { captureMode = it },
                                    )
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        val onTurnConfirmed: (List<DetectedThrow>) -> Unit =
                                            matchViewModel::confirmTurn
                                        when (captureMode) {
                                            CaptureMode.LIVE_CAMERA ->
                                                LiveScoringScreen(
                                                    cvEngine = appContainer.cvEngine,
                                                    onTurnConfirmed = onTurnConfirmed,
                                                )
                                            CaptureMode.PHOTO ->
                                                PhotoScoringScreen(
                                                    cvEngine = appContainer.cvEngine,
                                                    onTurnConfirmed = onTurnConfirmed,
                                                )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                            Text("Camera permission is required to score darts.")
                        }
                    }
                }
            }
        }
    }
}
