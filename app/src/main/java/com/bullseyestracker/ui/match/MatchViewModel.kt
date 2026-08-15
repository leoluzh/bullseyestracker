package com.bullseyestracker.ui.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bullseyestracker.cv.DetectedThrow
import com.bullseyestracker.cv.Ring
import com.bullseyestracker.match.data.MatchRepository
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.match.model.Player
import com.bullseyestracker.match.model.Throw
import com.bullseyestracker.match.model.ThrowRing
import com.bullseyestracker.match.model.Turn
import com.bullseyestracker.match.model.TurnOutcome
import com.bullseyestracker.match.rules.CricketRules
import com.bullseyestracker.match.rules.OpponentState
import com.bullseyestracker.match.rules.X501Rules
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Owns match state for the UI (spec 003-501-match / 004-cricket-match User Story 1): starts a
 * match, and turns confirmed [DetectedThrow]s (from either capture mode, FR-009) into
 * [X501Rules]/[CricketRules] outcomes — dispatched on [GameMode] — persisted via
 * [MatchRepository].
 */
class MatchViewModel(
    private val matchRepository: MatchRepository,
) : ViewModel() {
    private val _match = MutableStateFlow<Match?>(null)

    /** The current or just-completed match. Null only before any match has been started. */
    val match: StateFlow<Match?> = _match.asStateFlow()

    init {
        viewModelScope.launch {
            matchRepository.observeInProgressMatch().collect { inProgress ->
                // Only overwrite from an in-progress match; a COMPLETED match (winner banner)
                // is held locally by confirmTurn() below since observeInProgressMatch() only
                // ever returns IN_PROGRESS matches (see MatchDao).
                if (inProgress != null) _match.value = inProgress
            }
        }
    }

    fun startMatch(
        gameMode: GameMode,
        playerNames: List<String>,
    ) {
        viewModelScope.launch {
            _match.value = matchRepository.createMatch(gameMode, playerNames)
        }
    }

    /** Clears the completed match so the UI returns to [MatchSetupScreen] for a new one. */
    fun startNewMatch() {
        _match.value = null
    }

    fun confirmTurn(detectedThrows: List<DetectedThrow>) {
        val currentMatch = _match.value ?: return
        if (currentMatch.status == MatchStatus.COMPLETED) return // FR-008/FR-010

        val throws = detectedThrows.map { it.toMatchThrow() }
        when (currentMatch.gameMode) {
            GameMode.FIVE_O_ONE -> confirm501Turn(currentMatch, throws)
            GameMode.CRICKET -> confirmCricketTurn(currentMatch, throws)
        }
    }

    private fun confirm501Turn(
        currentMatch: Match,
        throws: List<Throw>,
    ) {
        val activePlayer = currentMatch.currentPlayer
        val remainingBefore = activePlayer.remainingScore ?: return

        val result = X501Rules.resolveTurn(remainingBefore, throws)
        val turn =
            Turn(
                id = UUID.randomUUID().toString(),
                matchId = currentMatch.id,
                playerId = activePlayer.id,
                throws = result.throwsUsed,
                outcome = result.outcome,
            )
        val updatedPlayer = activePlayer.copy(remainingScore = result.newRemainingScore)
        val nextPlayerIndex = (currentMatch.currentPlayerIndex + 1) % currentMatch.players.size
        val isCheckout = result.outcome == TurnOutcome.CHECKOUT
        val winnerId = if (isCheckout) activePlayer.id else null

        val updatedMatch =
            currentMatch.copy(
                players = currentMatch.players.map { if (it.id == updatedPlayer.id) updatedPlayer else it },
                currentPlayerIndex = nextPlayerIndex,
                status = if (isCheckout) MatchStatus.COMPLETED else MatchStatus.IN_PROGRESS,
                winnerId = winnerId,
            )

        viewModelScope.launch {
            matchRepository.saveTurn(currentMatch, turn, updatedPlayer, nextPlayerIndex, winnerId)
            _match.value = updatedMatch
        }
    }

    private fun confirmCricketTurn(
        currentMatch: Match,
        throws: List<Throw>,
    ) {
        val activePlayer = currentMatch.currentPlayer
        val opponents =
            currentMatch.players
                .filter { it.id != activePlayer.id }
                .map { OpponentState(it.marks, it.points) }

        val result = CricketRules.resolveTurn(activePlayer.marks, activePlayer.points, opponents, throws)
        val turn =
            Turn(
                id = UUID.randomUUID().toString(),
                matchId = currentMatch.id,
                playerId = activePlayer.id,
                throws = result.throwsUsed,
                outcome = if (result.isMatchWin) TurnOutcome.CHECKOUT else TurnOutcome.NORMAL,
            )
        val updatedPlayer: Player = activePlayer.copy(marks = result.newMarks, points = result.newPoints)
        val nextPlayerIndex = (currentMatch.currentPlayerIndex + 1) % currentMatch.players.size
        val winnerId = if (result.isMatchWin) activePlayer.id else null

        val updatedMatch =
            currentMatch.copy(
                players = currentMatch.players.map { if (it.id == updatedPlayer.id) updatedPlayer else it },
                currentPlayerIndex = nextPlayerIndex,
                status = if (result.isMatchWin) MatchStatus.COMPLETED else MatchStatus.IN_PROGRESS,
                winnerId = winnerId,
            )

        viewModelScope.launch {
            matchRepository.saveTurn(currentMatch, turn, updatedPlayer, nextPlayerIndex, winnerId)
            _match.value = updatedMatch
        }
    }

    private fun DetectedThrow.toMatchThrow(): Throw =
        Throw(
            id = UUID.randomUUID().toString(),
            sectorNumber = sectorNumber,
            ring = ring.toMatchRing(),
            value = value,
            confidence = confidence,
            wasManuallyCorrected = false,
        )

    private fun Ring.toMatchRing(): ThrowRing = ThrowRing.valueOf(name)
}

class MatchViewModelFactory(
    private val matchRepository: MatchRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MatchViewModel(matchRepository) as T
}
