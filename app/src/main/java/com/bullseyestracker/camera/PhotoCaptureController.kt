package com.bullseyestracker.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executors

/**
 * Drives the photo-capture screen (US2): binds preview + [ImageCapture] via [CameraController],
 * then turns a single `takePicture()` into a decoded, EXIF-rotated [Bitmap] ready to wrap in a
 * `FrameInput` for `CvEngine.detectThrows()` — the same entry point LiveDetectionAnalyzer uses,
 * so a captured photo scores identically to a live frame (see PhotoDetectionTest).
 */
class PhotoCaptureController(private val context: Context) {
    private val cameraController = CameraController(context)
    private val captureExecutor = Executors.newSingleThreadExecutor()

    fun bind(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        cameraController.bindPhotoCapture(lifecycleOwner, previewView)
    }

    fun capturePhoto(
        onCaptured: (Bitmap) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        val imageCapture = cameraController.currentImageCapture()
        if (imageCapture == null) {
            onError(IllegalStateException("Camera not bound yet — call bind() first"))
            return
        }

        val outputFile = File.createTempFile("dart-photo-", ".jpg", context.cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            captureExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = outputFile.decodeUprightBitmapOrNull()
                    outputFile.delete()
                    if (bitmap == null) {
                        onError(IllegalStateException("Failed to decode captured photo"))
                    } else {
                        onCaptured(bitmap)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    outputFile.delete()
                    onError(exception)
                }
            },
        )
    }
}

private fun File.decodeUprightBitmapOrNull(): Bitmap? {
    val bitmap = BitmapFactory.decodeFile(path) ?: return null
    val rotationDegrees =
        when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    if (rotationDegrees == 0) return bitmap
    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
