package com.bullseyestracker.match.stats

import com.bullseyestracker.match.model.Match

data class PlayerStats(
    val name: String,
    val matchesPlayed: Int,
    val matchesWon: Int,
) {
    val winRate: Float get() = if (matchesPlayed == 0) 0f else matchesWon.toFloat() / matchesPlayed
}

/**
 * Aggregates completed matches into per-player-name win/loss statistics (spec
 * 010-player-stats). There is no persistent cross-match player identity in this app's data
 * model — [com.bullseyestracker.match.model.Player.id] is generated fresh per match — so player
 * identity here is the trimmed, case-insensitive name (data-model.md), with the first
 * chronologically-seen trimmed spelling used for display.
 */
object PlayerStatsCalculator {
    /**
     * [matches] MUST already be completed-only (e.g. the output of
     * `MatchRepository.observeCompletedMatches()`) — this function does not filter by
     * `MatchStatus` itself (research.md).
     */
    fun compute(matches: List<Match>): List<PlayerStats> {
        data class Accumulator(
            val displayName: String,
            var matchesPlayed: Int = 0,
            var matchesWon: Int = 0,
        )

        val byKey = LinkedHashMap<String, Accumulator>()
        for (match in matches.sortedBy { it.startedAt }) {
            for (player in match.players) {
                val trimmedName = player.name.trim()
                val key = trimmedName.lowercase()
                val accumulator = byKey.getOrPut(key) { Accumulator(displayName = trimmedName) }
                accumulator.matchesPlayed++
                if (player.id == match.winnerId) accumulator.matchesWon++
            }
        }

        return byKey.values
            .map { PlayerStats(it.displayName, it.matchesPlayed, it.matchesWon) }
            .sortedWith(compareByDescending<PlayerStats> { it.winRate }.thenByDescending { it.matchesPlayed })
    }
}
