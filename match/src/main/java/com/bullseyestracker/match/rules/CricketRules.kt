package com.bullseyestracker.match.rules

import com.bullseyestracker.match.model.CricketNumber
import com.bullseyestracker.match.model.Throw
import com.bullseyestracker.match.model.ThrowRing

/** Another player's mark/point state, as needed to decide "still open"/win comparisons. */
data class OpponentState(
    val marks: Map<CricketNumber, Int>,
    val points: Int,
)

data class CricketTurnResult(
    val newMarks: Map<CricketNumber, Int>,
    val newPoints: Int,
    val throwsUsed: List<Throw>,
    val isMatchWin: Boolean,
)

/**
 * Pure, stateless Cricket rule logic (spec 004-cricket-match). Operates on one player's
 * marks/points in isolation, given a read-only snapshot of opponents' marks/points — the caller
 * (MatchViewModel) is responsible for advancing turns and persisting the result via
 * MatchRepository.
 */
object CricketRules {
    fun resolveTurn(
        marks: Map<CricketNumber, Int>,
        points: Int,
        opponents: List<OpponentState>,
        throws: List<Throw>,
    ): CricketTurnResult {
        val runningMarks = marks.toMutableMap()
        var runningPoints = points
        val newlyClosed = mutableSetOf<CricketNumber>()

        for (throwEntry in throws) {
            val target = targetNumber(throwEntry) ?: continue
            repeat(markWeight(throwEntry.ring)) {
                val current = runningMarks[target] ?: 0
                if (current < 3) {
                    runningMarks[target] = current + 1
                    if (runningMarks[target] == 3) newlyClosed.add(target)
                } else {
                    val opponentStillOpen = opponents.any { (it.marks[target] ?: 0) < 3 }
                    if (opponentStillOpen) runningPoints += target.pointValue
                }
            }
        }

        val allClosed = CricketNumber.entries.all { (runningMarks[it] ?: 0) >= 3 }
        val opponentsWithClosedNumber =
            opponents.filter { opponent -> newlyClosed.any { number -> (opponent.marks[number] ?: 0) >= 3 } }
        val isMatchWin =
            newlyClosed.isNotEmpty() && allClosed && opponentsWithClosedNumber.all { it.points < runningPoints }

        return CricketTurnResult(runningMarks, runningPoints, throws, isMatchWin)
    }

    private fun targetNumber(throwEntry: Throw): CricketNumber? =
        when (throwEntry.ring) {
            ThrowRing.OUTER_BULL, ThrowRing.INNER_BULL -> CricketNumber.BULL
            ThrowRing.SINGLE, ThrowRing.DOUBLE, ThrowRing.TRIPLE ->
                when (throwEntry.sectorNumber) {
                    15 -> CricketNumber.FIFTEEN
                    16 -> CricketNumber.SIXTEEN
                    17 -> CricketNumber.SEVENTEEN
                    18 -> CricketNumber.EIGHTEEN
                    19 -> CricketNumber.NINETEEN
                    20 -> CricketNumber.TWENTY
                    else -> null
                }
            ThrowRing.MISS -> null
        }

    private fun markWeight(ring: ThrowRing): Int =
        when (ring) {
            ThrowRing.SINGLE, ThrowRing.OUTER_BULL -> 1
            ThrowRing.DOUBLE, ThrowRing.INNER_BULL -> 2
            ThrowRing.TRIPLE -> 3
            ThrowRing.MISS -> 0
        }
}
