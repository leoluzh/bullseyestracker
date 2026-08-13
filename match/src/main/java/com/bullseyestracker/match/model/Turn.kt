package com.bullseyestracker.match.model

enum class TurnOutcome { NORMAL, BUST, CHECKOUT, MATCH_WIN }

data class Turn(
    val id: String,
    val matchId: String,
    val playerId: String,
    val throws: List<Throw>,
    val outcome: TurnOutcome,
    /** Reference to the captured frame the throws were derived from; null if fully manual. */
    val sourceFrameId: String? = null,
) {
    init {
        require(throws.size in 1..3) { "A turn has 1-3 throws (spec Assumptions)." }
    }
}
