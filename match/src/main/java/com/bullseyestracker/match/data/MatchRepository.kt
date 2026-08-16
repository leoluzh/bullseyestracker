package com.bullseyestracker.match.data

import android.content.Context
import androidx.room.Room
import com.bullseyestracker.match.model.DetectionFrame
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.match.model.Player
import com.bullseyestracker.match.model.Turn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MatchRepository(
    private val dao: MatchDao,
) {
    companion object {
        /**
         * Builds a repository backed by a real Room database. The only place `match` exposes
         * Room's own types beyond this module — callers (the `app` module) never construct or
         * reference the database directly (constitution Principle II's isolation approach,
         * applied to persistence the same way it's applied to the `cv` module).
         */
        fun create(context: Context): MatchRepository {
            val database =
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        BullseyesDatabase::class.java,
                        "bullseyestracker.db",
                    ).build()
            return MatchRepository(database.matchDao())
        }
    }

    fun observeInProgressMatch(): Flow<Match?> = dao.observeInProgressMatch().map { entity -> entity?.let { assembleMatch(it) } }

    fun observeCompletedMatches(): Flow<List<Match>> = dao.observeCompletedMatches().map { entities -> entities.map { assembleMatch(it) } }

    suspend fun createMatch(
        gameMode: GameMode,
        playerNames: List<String>,
    ): Match {
        require(playerNames.size in 2..4) { "A match requires 2-4 players (spec FR-007, SC-004)." }
        val matchId = UUID.randomUUID().toString()
        val playerEntities =
            playerNames.mapIndexed { index, name ->
                Player(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    remainingScore = if (gameMode == GameMode.FIVE_O_ONE) 501 else null,
                ).toEntity(matchId, index)
            }
        val matchEntity =
            MatchEntity(
                id = matchId,
                gameMode = gameMode,
                currentPlayerIndex = 0,
                status = MatchStatus.IN_PROGRESS,
                winnerId = null,
                startedAt = System.currentTimeMillis(),
                endedAt = null,
            )
        dao.insertMatch(matchEntity)
        dao.insertPlayers(playerEntities)
        return assembleMatch(matchEntity)
    }

    suspend fun getMatch(matchId: String): Match? = dao.getMatch(matchId)?.let { assembleMatch(it) }

    /**
     * Persists a confirmed turn (throws + updated player state + whose turn is next),
     * completing the match when [winnerId] is supplied. Called by the active game-mode's
     * rule engine (X501Rules/CricketRules) via MatchViewModel once a turn is confirmed.
     */
    suspend fun saveTurn(
        match: Match,
        turn: Turn,
        updatedPlayer: Player,
        nextPlayerIndex: Int,
        winnerId: String? = null,
    ) {
        val turnIndex = dao.getTurnCount(match.id)
        dao.insertTurn(turn.toEntity(match.id, turnIndex))
        dao.insertThrows(turn.throws.mapIndexed { i, t -> t.toEntity(turn.id, i) })

        val orderIndex = match.players.indexOfFirst { it.id == updatedPlayer.id }
        dao.updatePlayer(updatedPlayer.toEntity(match.id, orderIndex))

        val isCompleted = winnerId != null
        dao.updateMatch(
            MatchEntity(
                id = match.id,
                gameMode = match.gameMode,
                currentPlayerIndex = nextPlayerIndex,
                status = if (isCompleted) MatchStatus.COMPLETED else MatchStatus.IN_PROGRESS,
                winnerId = winnerId,
                startedAt = match.startedAt,
                endedAt = if (isCompleted) System.currentTimeMillis() else null,
            ),
        )
    }

    /** Discards an in-progress match entirely (not surfaced in history) so the player can back
     * out to the home screen without finishing it. */
    suspend fun abandonMatch(matchId: String) {
        dao.deleteMatch(matchId)
    }

    suspend fun saveDetectionFrame(frame: DetectionFrame) {
        dao.insertDetectionFrame(
            DetectionFrameEntity(
                id = frame.id,
                captureMode = frame.captureMode,
                imageRef = frame.imageRef,
                capturedAt = frame.capturedAt,
                boardDetected = frame.boardDetected,
            ),
        )
    }

    private suspend fun assembleMatch(entity: MatchEntity): Match {
        val players = dao.getPlayers(entity.id).map { it.toDomain() }
        return entity.toDomain(players)
    }
}
