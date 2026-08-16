package com.bullseyestracker.ui.calibration

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bullseyestracker.camera.CameraController
import com.bullseyestracker.camera.LiveDetectionAnalyzer
import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.ui.detection.BullseyeOverlay
import com.bullseyestracker.ui.theme.DartGold
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

private val SCRIM = Color.Black.copy(alpha = 0.55f)

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
    var statusAccent by remember { mutableStateOf(DartGold) }
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

        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.textButtonColors(containerColor = SCRIM, contentColor = Color.White),
        ) {
            Text("‹ Back", style = MaterialTheme.typography.bodyLarge)
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            color = SCRIM,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(10.dp).background(statusAccent, CircleShape))
                Text(
                    boardStatus,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }
    }
}
