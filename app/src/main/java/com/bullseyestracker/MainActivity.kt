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
import com.bullseyestracker.ui.detection.CaptureMode
import com.bullseyestracker.ui.detection.CaptureModeSelector
import com.bullseyestracker.ui.detection.LiveScoringScreen
import com.bullseyestracker.ui.detection.PhotoScoringScreen
import com.bullseyestracker.ui.match.CricketScoreboardScreen
import com.bullseyestracker.ui.match.FiveOOneScoreboardScreen
import com.bullseyestracker.ui.match.MatchSetupScreen
import com.bullseyestracker.ui.match.MatchViewModel
import com.bullseyestracker.ui.match.MatchViewModelFactory
import com.bullseyestracker.ui.theme.BullseyesTrackerTheme

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
            val matchViewModel: MatchViewModel =
                viewModel(factory = MatchViewModelFactory(appContainer.matchRepository))
            val match by matchViewModel.match.collectAsState()

            BullseyesTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (cameraPermissionGranted) {
                        val currentMatch = match
                        if (currentMatch == null) {
                            MatchSetupScreen(onStartMatch = matchViewModel::startMatch)
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
