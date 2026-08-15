package com.bullseyestracker.match.rules

import com.bullseyestracker.match.model.Throw
import com.bullseyestracker.match.model.ThrowRing
import com.bullseyestracker.match.model.TurnOutcome

data class TurnResult(
    val newRemainingScore: Int,
    val outcome: TurnOutcome,
    val throwsUsed: List<Throw>,
)

/**
 * Pure, stateless 501 rule logic (spec 003-501-match). Operates on one player's score in
 * isolation — the caller (MatchViewModel) is responsible for advancing turns and persisting the
 * result via MatchRepository.
 */
object X501Rules {
    fun resolveTurn(
        remainingScoreBefore: Int,
        throws: List<Throw>,
    ): TurnResult {
        var runningScore = remainingScoreBefore
        val throwsUsed = mutableListOf<Throw>()

        for (throwEntry in throws) {
            val newScore = runningScore - throwEntry.value
            throwsUsed.add(throwEntry)

            if (newScore < 0 || newScore == 1) {
                return TurnResult(remainingScoreBefore, TurnOutcome.BUST, throwsUsed)
            }
            if (newScore == 0) {
                val isDoubleOut = throwEntry.ring == ThrowRing.DOUBLE || throwEntry.ring == ThrowRing.INNER_BULL
                return if (isDoubleOut) {
                    TurnResult(0, TurnOutcome.CHECKOUT, throwsUsed)
                } else {
                    TurnResult(remainingScoreBefore, TurnOutcome.BUST, throwsUsed)
                }
            }

            runningScore = newScore
        }

        return TurnResult(runningScore, TurnOutcome.NORMAL, throwsUsed)
    }
}
