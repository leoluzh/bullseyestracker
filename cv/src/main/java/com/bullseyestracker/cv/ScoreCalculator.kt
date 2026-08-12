package com.bullseyestracker.cv

/**
 * Single source of truth for sector+ring -> point value, so no caller can store a value that
 * disagrees with the sector/ring it was derived from (data-model.md Throw validation rule).
 */
object ScoreCalculator {
    fun valueFor(sectorNumber: Int?, ring: Ring): Int = when (ring) {
        Ring.MISS -> 0
        Ring.INNER_BULL -> 50
        Ring.OUTER_BULL -> 25
        Ring.SINGLE -> sectorNumber ?: 0
        Ring.DOUBLE -> (sectorNumber ?: 0) * 2
        Ring.TRIPLE -> (sectorNumber ?: 0) * 3
    }
}
