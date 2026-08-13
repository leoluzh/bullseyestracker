package com.bullseyestracker.match.model

enum class ThrowRing { SINGLE, DOUBLE, TRIPLE, OUTER_BULL, INNER_BULL, MISS }

data class Throw(
    val id: String,
    /** 1-20, null for BULL rings or MISS. */
    val sectorNumber: Int?,
    val ring: ThrowRing,
    val value: Int,
    /** Null when the throw was entered fully manually (no detection backing it). */
    val confidence: Float?,
    val wasManuallyCorrected: Boolean,
)
