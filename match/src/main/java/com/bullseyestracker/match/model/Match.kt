package com.bullseyestracker.match.model

enum class GameMode { FIVE_O_ONE, CRICKET }

enum class MatchStatus { IN_PROGRESS, COMPLETED }

data class Match(
    val id: String,
    val gameMode: GameMode,
    val players: List<Player>,
    val currentPlayerIndex: Int,
    val status: MatchStatus,
    val winnerId: String? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
) {
    init {
        require(players.size in 2..4) { "A match requires 2-4 players (spec FR-007, SC-004)." }
        require(winnerId == null || status == MatchStatus.COMPLETED) {
            "winnerId must only be set once the match is COMPLETED."
        }
        require(winnerId == null || players.any { it.id == winnerId }) {
            "winnerId must reference a player in this match."
        }
    }

    val currentPlayer: Player get() = players[currentPlayerIndex]
}
