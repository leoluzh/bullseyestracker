package com.bullseyestracker.match.stats

import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.Match
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.match.model.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerStatsCalculatorTest {
    private fun player(
        id: String,
        name: String,
    ) = Player(id = id, name = name, remainingScore = 0)

    private fun completedMatch(
        id: String,
        players: List<Player>,
        winnerId: String,
        startedAt: Long,
    ) = Match(
        id = id,
        gameMode = GameMode.FIVE_O_ONE,
        players = players,
        currentPlayerIndex = 0,
        status = MatchStatus.COMPLETED,
        winnerId = winnerId,
        startedAt = startedAt,
        endedAt = startedAt + 1,
    )

    @Test
    fun `a name repeated across matches aggregates matches played and wins`() {
        val alice1 = player("a1", "Alice")
        val bob1 = player("b1", "Bob")
        val alice2 = player("a2", "Alice")
        val bob2 = player("b2", "Bob")
        val matches =
            listOf(
                completedMatch("m1", listOf(alice1, bob1), winnerId = "a1", startedAt = 1L),
                completedMatch("m2", listOf(alice2, bob2), winnerId = "b2", startedAt = 2L),
            )

        val stats = PlayerStatsCalculator.compute(matches)

        val alice = stats.single { it.name == "Alice" }
        assertEquals(2, alice.matchesPlayed)
        assertEquals(1, alice.matchesWon)
        assertEquals(0.5f, alice.winRate)
    }

    @Test
    fun `a player who has never won still appears with a zero win rate`() {
        val alice = player("a1", "Alice")
        val bob = player("b1", "Bob")
        val matches = listOf(completedMatch("m1", listOf(alice, bob), winnerId = "a1", startedAt = 1L))

        val stats = PlayerStatsCalculator.compute(matches)

        val bobStats = stats.single { it.name == "Bob" }
        assertEquals(1, bobStats.matchesPlayed)
        assertEquals(0, bobStats.matchesWon)
        assertEquals(0f, bobStats.winRate)
    }

    @Test
    fun `player name matching is trimmed and case-insensitive`() {
        val alice1 = player("a1", "Alice")
        val bob1 = player("b1", "Bob")
        val alice2 = player("a2", " alice ")
        val bob2 = player("b2", "Bob")
        val matches =
            listOf(
                completedMatch("m1", listOf(alice1, bob1), winnerId = "a1", startedAt = 1L),
                completedMatch("m2", listOf(alice2, bob2), winnerId = "b2", startedAt = 2L),
            )

        val stats = PlayerStatsCalculator.compute(matches)

        assertEquals(1, stats.count { it.name.equals("Alice", ignoreCase = true) })
        val alice = stats.single { it.name.equals("Alice", ignoreCase = true) }
        assertEquals(2, alice.matchesPlayed)
        assertEquals("Alice", alice.name)
    }

    @Test
    fun `results are ordered by win rate descending then matches played descending`() {
        val alice = player("a1", "Alice") // 1/1 = 100%
        val bob1 = player("b1", "Bob") // 1/2 = 50%, 2 played
        val bob2 = player("b2", "Bob")
        val carol1 = player("c1", "Carol") // 1/2 = 50%, 2 played (tied with Bob on rate)
        val carol2 = player("c2", "Carol")
        val dave = player("d1", "Dave") // 0/1 = 0%
        val matches =
            listOf(
                completedMatch("m1", listOf(alice, dave), winnerId = "a1", startedAt = 1L),
                completedMatch("m2", listOf(bob1, carol1), winnerId = "b1", startedAt = 2L),
                completedMatch("m3", listOf(bob2, carol2), winnerId = "c2", startedAt = 3L),
            )

        val stats = PlayerStatsCalculator.compute(matches)

        assertEquals(listOf("Alice", "Bob", "Carol", "Dave"), stats.map { it.name })
    }

    @Test
    fun `an empty match list produces an empty result`() {
        assertEquals(emptyList<PlayerStats>(), PlayerStatsCalculator.compute(emptyList()))
    }
}
