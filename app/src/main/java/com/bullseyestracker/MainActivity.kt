package com.bullseyestracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.bullseyestracker.di.AppContainer
import com.bullseyestracker.ui.detection.LiveScoringScreen
import com.bullseyestracker.ui.theme.BullseyesTrackerTheme
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer
    private var cameraPermissionGranted by mutableStateOf(false)

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraPermissionGranted = granted }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        check(OpenCVLoader.initLocal()) { "OpenCV native library failed to load" }
        appContainer = AppContainer(applicationContext)

        cameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!cameraPermissionGranted) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        setContent {
            BullseyesTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (cameraPermissionGranted) {
                        LiveScoringScreen(
                            cvEngine = appContainer.cvEngine,
                            onTurnConfirmed = {
                                // Match-mode wiring (US3/US4, MatchViewModel) not yet built —
                                // confirmed turns are discarded until that phase lands.
                            }
                        )
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
