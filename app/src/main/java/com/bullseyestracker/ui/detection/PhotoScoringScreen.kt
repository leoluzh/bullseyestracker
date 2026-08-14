package com.bullseyestracker.ui.detection

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bullseyestracker.camera.PhotoCaptureController
import com.bullseyestracker.cv.BoardCalibrationResult
import com.bullseyestracker.cv.CvEngine
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.cv.FrameInput

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
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var currentThrows by remember { mutableStateOf(listOf<DetectedThrow>()) }
    var correctingIndex by remember { mutableStateOf(-1) }

    val captureController = remember { PhotoCaptureController(context) }

    fun retake() {
        capturedPhoto = null
        currentThrows = emptyList()
        status = "Point the camera at the board, then take a photo"
    }

    fun scorePhoto(photo: Bitmap) {
        val frame = FrameInput(bitmap = photo)
        when (val calibrationResult = cvEngine.calibrateBoard(frame)) {
            is BoardCalibrationResult.Calibrated -> {
                currentThrows = cvEngine.detectThrows(frame, calibrationResult.calibration)
                status =
                    if (currentThrows.isEmpty()) {
                        "No darts found in the photo"
                    } else {
                        "Found ${currentThrows.size} dart(s) — review and confirm"
                    }
            }
            BoardCalibrationResult.NotFound -> {
                status = "No dartboard found in the photo — retake with the board in frame"
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
            DetectionOverlay(
                detections = currentThrows,
                modifier = Modifier.fillMaxSize(),
                onDetectionTapped = { tapped -> correctingIndex = currentThrows.indexOf(tapped) },
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(status)
            if (photo != null) {
                Text("Darts: ${currentThrows.size}/3 — total ${currentThrows.sumOf { it.value }}")
            }
            Row {
                if (photo == null) {
                    Button(onClick = {
                        captureController.capturePhoto(
                            onCaptured = { bitmap ->
                                capturedPhoto = bitmap
                                scorePhoto(bitmap)
                            },
                            onError = { status = "Photo capture failed — try again" },
                        )
                    }) { Text("Take photo") }
                } else {
                    Button(onClick = { retake() }) { Text("Retake") }
                    Button(
                        onClick = {
                            onTurnConfirmed(currentThrows)
                            retake()
                        },
                        enabled = currentThrows.isNotEmpty(),
                    ) { Text("Confirm turn") }
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
