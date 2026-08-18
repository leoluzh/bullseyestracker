package com.bullseyestracker.ui.detection

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bullseyestracker.camera.CameraController
import com.bullseyestracker.camera.LiveDetectionAnalyzer
import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.ui.theme.DartGold
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

private val SCRIM = Color.Black.copy(alpha = 0.55f)

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
    var statusAccent by remember { mutableStateOf(DartGold) }
    var currentThrows by remember { mutableStateOf(listOf<DetectedThrow>()) }
    var correctingIndex by remember { mutableStateOf(-1) }
    var calibration by remember { mutableStateOf<BoardCalibration?>(null) }

    val cameraController = remember { CameraController(context) }
    val analyzer =
        remember {
            LiveDetectionAnalyzer(
                cvEngine = cvEngine,
                onCalibrated = { newCalibration, confidence ->
                    calibration = newCalibration
                    if (confidence < 0.5f) {
                        boardStatus = "Board found (low confidence) — hold steady"
                        statusAccent = DartGold
                    } else {
                        boardStatus = "Board calibrated"
                        statusAccent = DartGreen
                    }
                },
                onBoardNotFound = {
                    calibration = null
                    boardStatus = "No dartboard found — point the camera at the board"
                    statusAccent = DartRed
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

        BullseyeOverlay(calibration = calibration, modifier = Modifier.fillMaxSize())

        DetectionOverlay(
            detections = currentThrows,
            modifier = Modifier.fillMaxSize(),
            onDetectionTapped = { tapped -> correctingIndex = currentThrows.indexOf(tapped) },
        )

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = SCRIM,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(statusAccent, CircleShape))
                    Text(
                        boardStatus,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Text(
                    "Darts this turn: ${currentThrows.size}/3 — total ${currentThrows.sumOf { it.value }}",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                // Small, unobtrusive indicator of which detection backend produced these
                // results (spec 014-dnn-dart-detection quickstart.md step 5 manual check).
                Text(
                    "Detection: ${cvEngine.detectionBackend.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Button(
                    onClick = {
                        onTurnConfirmed(currentThrows)
                        currentThrows = emptyList()
                    },
                    enabled = currentThrows.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text("Confirm turn")
                }
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
