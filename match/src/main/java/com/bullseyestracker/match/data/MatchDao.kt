package com.bullseyestracker.match.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurn(turn: TurnEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThrows(throws: List<ThrowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionFrame(frame: DetectionFrameEntity)

    @Query("SELECT * FROM matches WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun observeInProgressMatch(): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE status = 'COMPLETED' ORDER BY endedAt DESC")
    fun observeCompletedMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatch(matchId: String): MatchEntity?

    @Query("SELECT * FROM players WHERE matchId = :matchId ORDER BY orderIndex")
    suspend fun getPlayers(matchId: String): List<PlayerEntity>

    @Query("SELECT * FROM turns WHERE matchId = :matchId ORDER BY turnIndex")
    suspend fun getTurns(matchId: String): List<TurnEntity>

    @Query("SELECT * FROM throws WHERE turnId = :turnId ORDER BY throwIndex")
    suspend fun getThrowsForTurn(turnId: String): List<ThrowEntity>

    @Query("SELECT COUNT(*) FROM turns WHERE matchId = :matchId")
    suspend fun getTurnCount(matchId: String): Int
}
