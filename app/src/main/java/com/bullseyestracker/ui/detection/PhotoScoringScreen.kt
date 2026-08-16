package com.bullseyestracker.ui.detection

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bullseyestracker.camera.PhotoCaptureController
import com.bullseyestracker.cv.BoardCalibration
import com.bullseyestracker.cv.BoardCalibrationResult
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.cv.FrameInput
import com.bullseyestracker.ui.theme.DartGold
import com.bullseyestracker.ui.theme.DartGreen
import com.bullseyestracker.ui.theme.DartRed

private val SCRIM = Color.Black.copy(alpha = 0.55f)

/**
 * User Story 2: take one photo of the board instead of scoring live; every dart in that photo
 * is detected through the same `CvEngine` used by [LiveScoringScreen] (see PhotoDetectionTest —
 * a captured photo scores identically to an equivalent live frame). Reuses [DetectionOverlay]/
 * [CorrectionDialog] unchanged, per spec Acceptance Scenario 2 ("the same as in the live-camera
 * flow").
 */
@Composable
fun PhotoScoringScreen(
    cvEngine: CvEngine,
    onTurnConfirmed: (List<DetectedThrow>) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var status by remember { mutableStateOf("Point the camera at the board, then take a photo") }
    var statusAccent by remember { mutableStateOf(DartGold) }
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var currentThrows by remember { mutableStateOf(listOf<DetectedThrow>()) }
    var correctingIndex by remember { mutableStateOf(-1) }
    var photoCalibration by remember { mutableStateOf<BoardCalibration?>(null) }

    val captureController = remember { PhotoCaptureController(context) }

    fun retake() {
        capturedPhoto = null
        currentThrows = emptyList()
        photoCalibration = null
        status = "Point the camera at the board, then take a photo"
        statusAccent = DartGold
    }

    fun scorePhoto(photo: Bitmap) {
        val frame = FrameInput(bitmap = photo)
        when (val calibrationResult = cvEngine.calibrateBoard(frame)) {
            is BoardCalibrationResult.Calibrated -> {
                photoCalibration = calibrationResult.calibration
                currentThrows = cvEngine.detectThrows(frame, calibrationResult.calibration)
                if (currentThrows.isEmpty()) {
                    status = "No darts found in the photo"
                    statusAccent = DartRed
                } else {
                    status = "Found ${currentThrows.size} dart(s) — review and confirm"
                    statusAccent = DartGreen
                }
            }
            BoardCalibrationResult.NotFound -> {
                photoCalibration = null
                status = "No dartboard found in the photo — retake with the board in frame"
                statusAccent = DartRed
            }
        }
    }

    val photo = capturedPhoto

    Box(modifier = Modifier.fillMaxSize()) {
        if (photo == null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        captureController.bind(lifecycleOwner, previewView)
                    }
                },
            )
        } else {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = "Captured board photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            BullseyeOverlay(calibration = photoCalibration, modifier = Modifier.fillMaxSize())
            DetectionOverlay(
                detections = currentThrows,
                modifier = Modifier.fillMaxSize(),
                onDetectionTapped = { tapped -> correctingIndex = currentThrows.indexOf(tapped) },
            )
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = SCRIM,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(statusAccent, CircleShape))
                    Text(
                        status,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                if (photo != null) {
                    Text(
                        "Darts: ${currentThrows.size}/3 — total ${currentThrows.sumOf { it.value }}",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (photo == null) {
                    Button(
                        onClick = {
                            captureController.capturePhoto(
                                onCaptured = { bitmap ->
                                    capturedPhoto = bitmap
                                    scorePhoto(bitmap)
                                },
                                onError = {
                                    status = "Photo capture failed — try again"
                                    statusAccent = DartRed
                                },
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    ) { Text("Take photo") }
                } else {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        OutlinedButton(
                            onClick = { retake() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                        ) { Text("Retake") }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                onTurnConfirmed(currentThrows)
                                retake()
                            },
                            enabled = currentThrows.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                        ) { Text("Confirm turn") }
                    }
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
