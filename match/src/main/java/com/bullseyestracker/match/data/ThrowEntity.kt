package com.bullseyestracker.match.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.bullseyestracker.match.model.ThrowRing

@Entity(
    tableName = "throws",
    foreignKeys = [
        ForeignKey(
            entity = TurnEntity::class,
            parentColumns = ["id"],
            childColumns = ["turnId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ThrowEntity(
    @PrimaryKey val id: String,
    val turnId: String,
    val throwIndex: Int,
    val sectorNumber: Int?,
    val ring: ThrowRing,
    val value: Int,
    val confidence: Float?,
    val wasManuallyCorrected: Boolean
)
