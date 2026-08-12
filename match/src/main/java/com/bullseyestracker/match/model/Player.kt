package com.bullseyestracker.match.model

/** Numbers a Cricket player can mark: 15-20 plus the bull. */
enum class CricketNumber(val pointValue: Int) {
    FIFTEEN(15), SIXTEEN(16), SEVENTEEN(17), EIGHTEEN(18), NINETEEN(19), TWENTY(20), BULL(25)
}

data class Player(
    val id: String,
    val name: String,
    /** 501 mode only; null in Cricket matches. */
    val remainingScore: Int? = null,
    /** Cricket mode only; mark count (0-3+) per number. Empty in 501 matches. */
    val marks: Map<CricketNumber, Int> = emptyMap(),
    /** Cricket mode only; points scored from marks on numbers an opponent still has open. */
    val points: Int = 0
) {
    fun isNumberClosed(number: CricketNumber): Boolean = (marks[number] ?: 0) >= 3
}
