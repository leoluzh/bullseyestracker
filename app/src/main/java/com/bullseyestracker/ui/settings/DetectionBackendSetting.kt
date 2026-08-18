package com.bullseyestracker.ui.settings

import android.content.Context
import com.bullseyestracker.cv.DetectionBackend

private const val PREFS_NAME = "settings"
private const val KEY_DETECTION_BACKEND = "detection_backend"

/**
 * Persists the user's [DetectionBackend] choice (spec 014-dnn-dart-detection FR-003/FR-004)
 * across app restarts. `SharedPreferences` is the simplest fit for this app's single boolean-ish
 * setting — no other persisted user preference exists yet to justify a heavier mechanism
 * (DataStore, etc.).
 */
class DetectionBackendSetting(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): DetectionBackend {
        val name = prefs.getString(KEY_DETECTION_BACKEND, null) ?: return DetectionBackend.CLASSICAL
        return runCatching { DetectionBackend.valueOf(name) }.getOrDefault(DetectionBackend.CLASSICAL)
    }

    fun set(backend: DetectionBackend) {
        prefs.edit().putString(KEY_DETECTION_BACKEND, backend.name).apply()
    }
}
