package com.bullseyestracker.cv.fixtures

import com.bullseyestracker.cv.Ring
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FixtureGroundTruthTest {
    @Test
    fun `parses valid ground truth with multiple darts`() {
        val json =
            """
            {
              "scenario": "three darts, normal lighting",
              "lighting": "normal",
              "darts": [
                { "sectorNumber": 20, "ring": "TRIPLE" },
                { "sectorNumber": 5, "ring": "SINGLE" },
                { "sectorNumber": null, "ring": "INNER_BULL" }
              ]
            }
            """.trimIndent()

        val result = FixtureGroundTruthParser.parse("three_darts", json)

        assertEquals("three_darts", result.fixtureName)
        assertEquals(Lighting.NORMAL, result.lighting)
        assertEquals(
            listOf(
                DartGroundTruth(20, Ring.TRIPLE),
                DartGroundTruth(5, Ring.SINGLE),
                DartGroundTruth(null, Ring.INNER_BULL),
            ),
            result.darts,
        )
    }

    @Test
    fun `parses valid ground truth with zero darts`() {
        val json = """{ "scenario": "empty board", "lighting": "dim", "darts": [] }"""

        val result = FixtureGroundTruthParser.parse("empty_board", json)

        assertEquals(Lighting.DIM, result.lighting)
        assertEquals(emptyList<DartGroundTruth>(), result.darts)
    }

    @Test
    fun `rejects unknown ring name`() {
        val json = """{ "scenario": "x", "lighting": "normal", "darts": [{ "sectorNumber": 5, "ring": "BULLSEYE" }] }"""

        assertThrows(InvalidFixtureGroundTruthException::class.java) {
            FixtureGroundTruthParser.parse("bad_ring", json)
        }
    }

    @Test
    fun `rejects sectorNumber outside 1 to 20`() {
        val json = """{ "scenario": "x", "lighting": "normal", "darts": [{ "sectorNumber": 21, "ring": "SINGLE" }] }"""

        assertThrows(InvalidFixtureGroundTruthException::class.java) {
            FixtureGroundTruthParser.parse("bad_sector", json)
        }
    }

    @Test
    fun `rejects a sector number paired with a bull ring`() {
        val json = """{ "scenario": "x", "lighting": "normal", "darts": [{ "sectorNumber": 5, "ring": "INNER_BULL" }] }"""

        assertThrows(InvalidFixtureGroundTruthException::class.java) {
            FixtureGroundTruthParser.parse("sector_with_bull", json)
        }
    }

    @Test
    fun `rejects a missing sector number for a single ring`() {
        val json = """{ "scenario": "x", "lighting": "normal", "darts": [{ "sectorNumber": null, "ring": "SINGLE" }] }"""

        assertThrows(InvalidFixtureGroundTruthException::class.java) {
            FixtureGroundTruthParser.parse("missing_sector", json)
        }
    }

    @Test
    fun `rejects an invalid lighting value`() {
        val json = """{ "scenario": "x", "lighting": "bright", "darts": [] }"""

        assertThrows(InvalidFixtureGroundTruthException::class.java) {
            FixtureGroundTruthParser.parse("bad_lighting", json)
        }
    }

    @Test
    fun `rejects malformed JSON`() {
        assertThrows(InvalidFixtureGroundTruthException::class.java) {
            FixtureGroundTruthParser.parse("malformed", "{ not json")
        }
    }

    @Test
    fun `findOrphanedFixtures reports png or json files with no matching pair`() {
        val orphans =
            findOrphanedFixtures(
                pngBaseNames = setOf("empty_board", "one_dart", "orphan_photo"),
                jsonBaseNames = setOf("empty_board", "one_dart", "orphan_label"),
            )

        assertEquals(listOf("orphan_label", "orphan_photo"), orphans)
    }

    @Test
    fun `findOrphanedFixtures returns empty when every png has a matching json`() {
        val orphans =
            findOrphanedFixtures(
                pngBaseNames = setOf("empty_board", "one_dart"),
                jsonBaseNames = setOf("empty_board", "one_dart"),
            )

        assertEquals(emptyList<String>(), orphans)
    }
}
