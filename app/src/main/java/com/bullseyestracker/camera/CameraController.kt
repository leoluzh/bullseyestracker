package com.bullseyestracker.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

/**
 * Binds CameraX use cases (preview + analysis + capture) to a lifecycle. Kept UI-agnostic
 * (no Composable/View coupling beyond the PreviewView surface) so it can back both the
 * live-scoring screen (ImageAnalysis) and the photo-capture screen (ImageCapture).
 */
class CameraController(private val context: Context) {
    private var imageCapture: ImageCapture? = null

    // Dedicated background executor for frame analysis — must never run on the main/UI thread
    // (constitution Principle IV: camera pipeline MUST NOT block the UI thread).
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    fun bindLiveDetection(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        analyzer: ImageAnalysis.Analyzer,
    ) {
        val providerFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            val provider = providerFuture.get()

            val preview =
                Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val analysis =
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(analysisExecutor, analyzer) }

            val capture = ImageCapture.Builder().build()
            imageCapture = capture

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
                capture,
            )
        }, ContextCompat.getMainExecutor(context))
    }

    fun currentImageCapture(): ImageCapture? = imageCapture
}
