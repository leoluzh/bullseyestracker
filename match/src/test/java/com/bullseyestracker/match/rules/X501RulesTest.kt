package com.bullseyestracker.match.rules

import com.bullseyestracker.match.model.ThrowRing
import com.bullseyestracker.match.model.TurnOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

class X501RulesTest {
    private fun throwOf(
        value: Int,
        ring: ThrowRing = ThrowRing.SINGLE,
        sectorNumber: Int? = null,
    ) = com.bullseyestracker.match.model.Throw(
        id = "t",
        sectorNumber = sectorNumber,
        ring = ring,
        value = value,
        confidence = 0.9f,
        wasManuallyCorrected = false,
    )

    @Test
    fun `normal turn subtracts throw values from remaining score`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 501,
                throws = listOf(throwOf(60, ThrowRing.TRIPLE, 20), throwOf(60, ThrowRing.TRIPLE, 20), throwOf(60, ThrowRing.TRIPLE, 20)),
            )

        assertEquals(501 - 180, result.newRemainingScore)
        assertEquals(TurnOutcome.NORMAL, result.outcome)
    }

    @Test
    fun `overshooting remaining score is a bust and score reverts to pre-turn value`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 20,
                throws = listOf(throwOf(25, ThrowRing.OUTER_BULL)),
            )

        assertEquals(TurnOutcome.BUST, result.outcome)
        assertEquals(20, result.newRemainingScore)
    }

    @Test
    fun `landing on exactly 1 remaining is a bust because a double can never finish on 1`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 3,
                throws = listOf(throwOf(2, ThrowRing.SINGLE, 2)),
            )

        assertEquals(TurnOutcome.BUST, result.outcome)
        assertEquals(3, result.newRemainingScore)
    }

    @Test
    fun `reaching exactly 0 on a double is a checkout win`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 40,
                throws = listOf(throwOf(40, ThrowRing.DOUBLE, 20)),
            )

        assertEquals(TurnOutcome.CHECKOUT, result.outcome)
        assertEquals(0, result.newRemainingScore)
    }

    @Test
    fun `reaching exactly 0 on the inner bull counts as a double-out checkout win`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 50,
                throws = listOf(throwOf(50, ThrowRing.INNER_BULL)),
            )

        assertEquals(TurnOutcome.CHECKOUT, result.outcome)
        assertEquals(0, result.newRemainingScore)
    }

    @Test
    fun `reaching exactly 0 on a non-double is a bust, not a checkout`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 20,
                throws = listOf(throwOf(20, ThrowRing.SINGLE, 20)),
            )

        assertEquals(TurnOutcome.BUST, result.outcome)
        assertEquals(20, result.newRemainingScore)
    }

    @Test
    fun `turn resolves early on checkout even if fewer than 3 darts were thrown`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 40,
                throws = listOf(throwOf(40, ThrowRing.DOUBLE, 20)),
            )

        assertEquals(1, result.throwsUsed.size)
    }

    @Test
    fun `turn resolves early on bust, ignoring any darts after the busting one`() {
        val result =
            X501Rules.resolveTurn(
                remainingScoreBefore = 20,
                throws =
                    listOf(
                        throwOf(25, ThrowRing.OUTER_BULL),
                        throwOf(20, ThrowRing.SINGLE, 20),
                        throwOf(20, ThrowRing.SINGLE, 20),
                    ),
            )

        assertEquals(1, result.throwsUsed.size)
        assertEquals(20, result.newRemainingScore)
    }
}
