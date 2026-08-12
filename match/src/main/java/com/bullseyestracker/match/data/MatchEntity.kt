package com.bullseyestracker.match.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.MatchStatus

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val gameMode: GameMode,
    val currentPlayerIndex: Int,
    val status: MatchStatus,
    val winnerId: String?,
    val startedAt: Long,
    val endedAt: Long?
)
