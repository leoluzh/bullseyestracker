package com.bullseyestracker.match.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bullseyestracker.match.model.CaptureMode

@Entity(tableName = "detection_frames")
data class DetectionFrameEntity(
    @PrimaryKey val id: String,
    val captureMode: CaptureMode,
    val imageRef: String,
    val capturedAt: Long,
    val boardDetected: Boolean,
)
