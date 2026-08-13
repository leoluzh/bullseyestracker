package com.bullseyestracker.cv

import org.opencv.android.OpenCVLoader

/**
 * Sole point where the `app` module touches anything OpenCV-related — loading the native
 * library at process start. Keeps the `org.opencv` dependency itself internal to this module
 * (constitution Principle II) instead of `app` needing its own OpenCV dependency just to call
 * OpenCVLoader directly.
 */
object CvNativeInit {
    fun load(): Boolean = OpenCVLoader.initLocal()
}
