package com.bullseyestracker.match.rules

import com.bullseyestracker.match.model.CricketNumber
import com.bullseyestracker.match.model.ThrowRing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CricketRulesTest {
    private fun throwOf(
        ring: ThrowRing,
        sectorNumber: Int? = null,
    ) = com.bullseyestracker.match.model.Throw(
        id = "t",
        sectorNumber = sectorNumber,
        ring = ring,
        value = 0,
        confidence = 0.9f,
        wasManuallyCorrected = false,
    )

    private fun opponent(
        marks: Map<CricketNumber, Int>,
        points: Int = 0,
    ) = OpponentState(marks, points)

    private val allNumbersClosed =
        mapOf(
            CricketNumber.FIFTEEN to 3,
            CricketNumber.SIXTEEN to 3,
            CricketNumber.SEVENTEEN to 3,
            CricketNumber.EIGHTEEN to 3,
            CricketNumber.NINETEEN to 3,
            CricketNumber.TWENTY to 3,
            CricketNumber.BULL to 3,
        )

    // BULL starts at 1 mark so a single inner-bull dart (2 marks) closes it exactly at 3.
    private val allClosedExceptBull = allNumbersClosed + (CricketNumber.BULL to 1)

    @Test
    fun `single dart adds one mark to its number`() {
        val result =
            CricketRules.resolveTurn(
                marks = emptyMap(),
                points = 0,
                opponents = listOf(opponent(emptyMap())),
                throws = listOf(throwOf(ThrowRing.SINGLE, 20)),
            )

        assertEquals(1, result.newMarks[CricketNumber.TWENTY])
    }

    @Test
    fun `double dart adds two marks to its number`() {
        val result =
            CricketRules.resolveTurn(
                marks = emptyMap(),
                points = 0,
                opponents = listOf(opponent(emptyMap())),
                throws = listOf(throwOf(ThrowRing.DOUBLE, 19)),
            )

        assertEquals(2, result.newMarks[CricketNumber.NINETEEN])
    }

    @Test
    fun `triple dart adds three marks to its number and closes it`() {
        val result =
            CricketRules.resolveTurn(
                marks = emptyMap(),
                points = 0,
                opponents = listOf(opponent(emptyMap())),
                throws = listOf(throwOf(ThrowRing.TRIPLE, 20)),
            )

        assertEquals(3, result.newMarks[CricketNumber.TWENTY])
    }

    @Test
    fun `outer bull adds one mark to BULL`() {
        val result =
            CricketRules.resolveTurn(
                marks = emptyMap(),
                points = 0,
                opponents = listOf(opponent(emptyMap())),
                throws = listOf(throwOf(ThrowRing.OUTER_BULL)),
            )

        assertEquals(1, result.newMarks[CricketNumber.BULL])
    }

    @Test
    fun `inner bull adds two marks to BULL`() {
        val result =
            CricketRules.resolveTurn(
                marks = emptyMap(),
                points = 0,
                opponents = listOf(opponent(emptyMap())),
                throws = listOf(throwOf(ThrowRing.INNER_BULL)),
            )

        assertEquals(2, result.newMarks[CricketNumber.BULL])
    }

    @Test
    fun `a dart that pushes marks from 2 to 5 closes the number at 3`() {
        val result =
            CricketRules.resolveTurn(
                marks = mapOf(CricketNumber.TWENTY to 2),
                points = 0,
                opponents = listOf(opponent(mapOf(CricketNumber.TWENTY to 0))),
                throws = listOf(throwOf(ThrowRing.TRIPLE, 20)),
            )

        assertTrue(result.newMarks[CricketNumber.TWENTY]!! >= 3)
    }

    @Test
    fun `dart outside 15-20 and bull is a no-op`() {
        val result =
            CricketRules.resolveTurn(
                marks = emptyMap(),
                points = 0,
                opponents = listOf(opponent(emptyMap())),
                throws = listOf(throwOf(ThrowRing.SINGLE, 5)),
            )

        assertTrue(result.newMarks.isEmpty() || result.newMarks.values.all { it == 0 })
        assertEquals(0, result.newPoints)
    }

    @Test
    fun `marks on an already-closed number score points while an opponent is still open`() {
        val result =
            CricketRules.resolveTurn(
                marks = mapOf(CricketNumber.TWENTY to 3),
                points = 0,
                opponents = listOf(opponent(mapOf(CricketNumber.TWENTY to 0))),
                throws = listOf(throwOf(ThrowRing.SINGLE, 20)),
            )

        assertEquals(20, result.newPoints)
        // marks don't accumulate past 3 — a closed number's count is capped, not tracked further
        assertEquals(3, result.newMarks[CricketNumber.TWENTY])
    }

    @Test
    fun `one triple dart can both close a number and score overflow points`() {
        val result =
            CricketRules.resolveTurn(
                marks = mapOf(CricketNumber.TWENTY to 2),
                points = 0,
                opponents = listOf(opponent(mapOf(CricketNumber.TWENTY to 0))),
                throws = listOf(throwOf(ThrowRing.TRIPLE, 20)),
            )

        // 1st mark closes (2 -> 3), remaining 2 marks score points (opponent still open)
        assertEquals(40, result.newPoints)
    }

    @Test
    fun `inner bull on an already-closed bull scores 25 points per mark while opponent open`() {
        val result =
            CricketRules.resolveTurn(
                marks = mapOf(CricketNumber.BULL to 3),
                points = 0,
                opponents = listOf(opponent(mapOf(CricketNumber.BULL to 0))),
                throws = listOf(throwOf(ThrowRing.INNER_BULL)),
            )

        assertEquals(50, result.newPoints)
    }

    @Test
    fun `marks on a number every player has closed are a pure no-op`() {
        val result =
            CricketRules.resolveTurn(
                marks = mapOf(CricketNumber.TWENTY to 3),
                points = 0,
                opponents = listOf(opponent(mapOf(CricketNumber.TWENTY to 3))),
                throws = listOf(throwOf(ThrowRing.SINGLE, 20)),
            )

        assertEquals(0, result.newPoints)
    }

    @Test
    fun `closing the last number with a strict points lead wins the match`() {
        val result =
            CricketRules.resolveTurn(
                marks = allClosedExceptBull,
                points = 10,
                opponents = listOf(opponent(allNumbersClosed, points = 5)),
                throws = listOf(throwOf(ThrowRing.INNER_BULL)),
            )

        assertTrue(result.isMatchWin)
    }

    @Test
    fun `closing the last number without the points lead does not win`() {
        val result =
            CricketRules.resolveTurn(
                marks = allClosedExceptBull,
                points = 0,
                opponents = listOf(opponent(allNumbersClosed, points = 5)),
                throws = listOf(throwOf(ThrowRing.INNER_BULL)),
            )

        assertFalse(result.isMatchWin)
    }

    @Test
    fun `closing the last number tied on points does not win`() {
        val result =
            CricketRules.resolveTurn(
                marks = allClosedExceptBull,
                points = 5,
                opponents = listOf(opponent(allNumbersClosed, points = 5)),
                throws = listOf(throwOf(ThrowRing.INNER_BULL)),
            )

        assertFalse(result.isMatchWin)
    }

    @Test
    fun `still having one number open does not win even with the points lead`() {
        val marksWithFifteenOpen = allNumbersClosed + (CricketNumber.FIFTEEN to 2)
        val result =
            CricketRules.resolveTurn(
                marks = marksWithFifteenOpen,
                points = 100,
                opponents = listOf(opponent(emptyMap())),
                // scores points on the already-closed 20 — doesn't touch FIFTEEN, still open
                throws = listOf(throwOf(ThrowRing.SINGLE, 20)),
            )

        assertFalse(result.isMatchWin)
    }
}
