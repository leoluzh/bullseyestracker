package com.bullseyestracker.ui.detection

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
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.cv.DetectedThrow

/**
 * User Story 1: point the camera at the board; detected darts are overlaid and scored live.
 * Standalone/independently testable per spec — this screen just accumulates and surfaces the
 * turn's detected throws via [onTurnConfirmed]; match-mode wiring (US3/US4) happens in the
 * caller, not here (constitution Principle II — this screen has no game-rule knowledge).
 */
@Composable
fun LiveScoringScreen(
    cvEngine: CvEngine,
    onTurnConfirmed: (List<DetectedThrow>) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var boardStatus by remember { mutableStateOf("Point the camera at the board…") }
    var currentThrows by remember { mutableStateOf(listOf<DetectedThrow>()) }
    var correctingIndex by remember { mutableStateOf(-1) }

    val cameraController = remember { CameraController(context) }
    val analyzer =
        remember {
            LiveDetectionAnalyzer(
                cvEngine = cvEngine,
                onCalibrated = { _, confidence ->
                    boardStatus =
                        if (confidence < 0.5f) {
                            "Board found (low confidence) — hold steady"
                        } else {
                            "Board calibrated"
                        }
                },
                onBoardNotFound = {
                    boardStatus = "No dartboard found — point the camera at the board"
                },
                onDetections = { detections ->
                    // DartDetector only reports newly-appeared darts per call, so appending (not
                    // replacing) accumulates the turn; takeLast(3) caps it if more than 3 land.
                    currentThrows = (currentThrows + detections).takeLast(3)
                },
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

        DetectionOverlay(
            detections = currentThrows,
            modifier = Modifier.fillMaxSize(),
            onDetectionTapped = { tapped -> correctingIndex = currentThrows.indexOf(tapped) },
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(boardStatus)
            Text("Darts this turn: ${currentThrows.size}/3 — total ${currentThrows.sumOf { it.value }}")
            Button(
                onClick = {
                    onTurnConfirmed(currentThrows)
                    currentThrows = emptyList()
                },
                enabled = currentThrows.isNotEmpty(),
            ) {
                Text("Confirm turn")
            }
        }
    }

    if (correctingIndex in currentThrows.indices) {
        CorrectionDialog(
            initial = currentThrows[correctingIndex],
            onConfirm = { corrected ->
                currentThrows = currentThrows.toMutableList().also { it[correctingIndex] = corrected }
                correctingIndex = -1
            },
            onDismiss = { correctingIndex = -1 },
        )
    }
}
