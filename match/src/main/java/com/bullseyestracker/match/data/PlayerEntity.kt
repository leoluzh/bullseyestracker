package com.bullseyestracker.match.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.bullseyestracker.match.model.CricketNumber

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlayerEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val name: String,
    val orderIndex: Int,
    val remainingScore: Int?,
    val marks: Map<CricketNumber, Int>,
    val points: Int
)
