package com.bullseyestracker.ui.calibration

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bullseyestracker.camera.CameraController
import com.bullseyestracker.camera.LiveDetectionAnalyzer
import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.ui.detection.BullseyeOverlay

/**
 * Standalone calibration test screen (spec 013-app-home-navigation User Story 4): runs the same
 * live board-calibration pipeline as [com.bullseyestracker.ui.detection.LiveScoringScreen] and
 * draws the same [BullseyeOverlay], but ignores dart-throw detections entirely (empty
 * `onDetections`) — no turn/score UI, no match created (FR-008).
 */
@Composable
fun CalibrationTestScreen(
    cvEngine: CvEngine,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var boardStatus by remember { mutableStateOf("Point the camera at the board…") }
    var calibration by remember { mutableStateOf<BoardCalibration?>(null) }

    val cameraController = remember { CameraController(context) }
    val analyzer =
        remember {
            LiveDetectionAnalyzer(
                cvEngine = cvEngine,
                onCalibrated = { newCalibration, confidence ->
                    calibration = newCalibration
                    boardStatus =
                        if (confidence < 0.5f) {
                            "Board found (low confidence) — hold steady"
                        } else {
                            "Board calibrated"
                        }
                },
                onBoardNotFound = {
                    calibration = null
                    boardStatus = "No dartboard found — point the camera at the board"
                },
                onDetections = { },
            )
        }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    cameraController.bindLiveDetection(lifecycleOwner, previewView, analyzer)
                }
            },
        )

        BullseyeOverlay(calibration = calibration, modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Text(boardStatus, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
