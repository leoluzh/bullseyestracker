package com.bullseyestracker.cv.fixtures

import com.bullseyestracker.cv.Ring
import org.json.JSONException
import org.json.JSONObject

enum class Lighting { NORMAL, DIM }

data class DartGroundTruth(
    val sectorNumber: Int?,
    val ring: Ring,
)

data class FixtureGroundTruth(
    val fixtureName: String,
    val scenario: String,
    val lighting: Lighting,
    val darts: List<DartGroundTruth>,
)

class InvalidFixtureGroundTruthException(
    message: String,
) : Exception(message)

/** Rings that never carry a sector number — mirrors [com.bullseyestracker.cv.DetectedThrow]'s own invariant. */
private val SECTORLESS_RINGS = setOf(Ring.INNER_BULL, Ring.OUTER_BULL, Ring.MISS)

/**
 * Parses and validates a fixture's ground-truth JSON (data-model.md), spec 006-fixture-benchmark
 * FR-001/FR-002. Pure string-in/data-out — no file or asset I/O, so it's plain-JVM unit-testable
 * (see research.md on why [DetectionAccuracyBenchmarkTest] itself must stay `androidTest`, while
 * this parsing/validation logic doesn't need to be).
 */
object FixtureGroundTruthParser {
    fun parse(
        fixtureName: String,
        json: String,
    ): FixtureGroundTruth {
        val obj =
            try {
                JSONObject(json)
            } catch (e: JSONException) {
                throw InvalidFixtureGroundTruthException("Fixture '$fixtureName': malformed JSON (${e.message})")
            }

        val scenario = obj.optString("scenario", "")
        if (scenario.isBlank()) {
            throw InvalidFixtureGroundTruthException("Fixture '$fixtureName': 'scenario' must not be blank")
        }

        val lighting =
            when (val raw = obj.optString("lighting", "")) {
                "normal" -> Lighting.NORMAL
                "dim" -> Lighting.DIM
                else -> throw InvalidFixtureGroundTruthException(
                    "Fixture '$fixtureName': 'lighting' must be \"normal\" or \"dim\", was \"$raw\"",
                )
            }

        val dartsArray =
            obj.optJSONArray("darts")
                ?: throw InvalidFixtureGroundTruthException("Fixture '$fixtureName': missing 'darts' array")

        val darts =
            (0 until dartsArray.length()).map { index ->
                val dartObj = dartsArray.getJSONObject(index)
                val ringRaw = dartObj.optString("ring", "")
                val ring =
                    try {
                        Ring.valueOf(ringRaw)
                    } catch (e: IllegalArgumentException) {
                        throw InvalidFixtureGroundTruthException(
                            "Fixture '$fixtureName': dart $index has unknown ring \"$ringRaw\"",
                        )
                    }
                val sectorNumber =
                    if (dartObj.isNull("sectorNumber") || !dartObj.has("sectorNumber")) {
                        null
                    } else {
                        dartObj.getInt("sectorNumber")
                    }

                validateSectorRingPair(fixtureName, index, sectorNumber, ring)
                DartGroundTruth(sectorNumber, ring)
            }

        return FixtureGroundTruth(fixtureName, scenario, lighting, darts)
    }

    private fun validateSectorRingPair(
        fixtureName: String,
        dartIndex: Int,
        sectorNumber: Int?,
        ring: Ring,
    ) {
        if (ring in SECTORLESS_RINGS) {
            if (sectorNumber != null) {
                throw InvalidFixtureGroundTruthException(
                    "Fixture '$fixtureName': dart $dartIndex has ring $ring but a non-null sectorNumber ($sectorNumber)",
                )
            }
        } else {
            if (sectorNumber == null) {
                throw InvalidFixtureGroundTruthException(
                    "Fixture '$fixtureName': dart $dartIndex has ring $ring but no sectorNumber",
                )
            }
            if (sectorNumber !in 1..20) {
                throw InvalidFixtureGroundTruthException(
                    "Fixture '$fixtureName': dart $dartIndex has sectorNumber $sectorNumber, must be 1..20",
                )
            }
        }
    }
}

/**
 * Pure set comparison (spec Edge Cases: a `.png` with no matching `.json`, or vice versa) — no
 * asset/file I/O, so it's usable from both the plain-JVM unit test and
 * [com.bullseyestracker.cv.DetectionAccuracyBenchmarkTest] (which supplies the real asset
 * listing).
 */
fun findOrphanedFixtures(
    pngBaseNames: Set<String>,
    jsonBaseNames: Set<String>,
): List<String> = ((pngBaseNames - jsonBaseNames) + (jsonBaseNames - pngBaseNames)).sorted()
