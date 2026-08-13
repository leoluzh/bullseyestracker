package com.bullseyestracker.match.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.bullseyestracker.match.model.TurnOutcome

@Entity(
    tableName = "turns",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TurnEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val playerId: String,
    val turnIndex: Int,
    val outcome: TurnOutcome,
    val sourceFrameId: String?,
)
