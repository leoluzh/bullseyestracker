package com.bullseyestracker.match.data

import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.Player
import com.bullseyestracker.match.model.Throw
import com.bullseyestracker.match.model.Turn

internal fun MatchEntity.toDomain(players: List<Player>): Match = Match(
    id = id,
    gameMode = gameMode,
    players = players,
    currentPlayerIndex = currentPlayerIndex,
    status = status,
    winnerId = winnerId,
    startedAt = startedAt,
    endedAt = endedAt
)

internal fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    name = name,
    remainingScore = remainingScore,
    marks = marks,
    points = points
)

internal fun Player.toEntity(matchId: String, orderIndex: Int): PlayerEntity = PlayerEntity(
    id = id,
    matchId = matchId,
    name = name,
    orderIndex = orderIndex,
    remainingScore = remainingScore,
    marks = marks,
    points = points
)

internal fun Turn.toEntity(matchId: String, turnIndex: Int): TurnEntity = TurnEntity(
    id = id,
    matchId = matchId,
    playerId = playerId,
    turnIndex = turnIndex,
    outcome = outcome,
    sourceFrameId = sourceFrameId
)

internal fun Throw.toEntity(turnId: String, throwIndex: Int): ThrowEntity = ThrowEntity(
    id = id,
    turnId = turnId,
    throwIndex = throwIndex,
    sectorNumber = sectorNumber,
    ring = ring,
    value = value,
    confidence = confidence,
    wasManuallyCorrected = wasManuallyCorrected
)
