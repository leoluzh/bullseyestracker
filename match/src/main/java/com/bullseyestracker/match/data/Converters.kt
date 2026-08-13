package com.bullseyestracker.match.data

import androidx.room.TypeConverter
import com.bullseyestracker.match.model.CaptureMode
import com.bullseyestracker.match.model.CricketNumber
import com.bullseyestracker.match.model.GameMode
import com.bullseyestracker.match.model.MatchStatus
import com.bullseyestracker.match.model.ThrowRing
import com.bullseyestracker.match.model.TurnOutcome

class Converters {
    @TypeConverter fun gameModeToString(v: GameMode): String = v.name

    @TypeConverter fun stringToGameMode(v: String): GameMode = GameMode.valueOf(v)

    @TypeConverter fun matchStatusToString(v: MatchStatus): String = v.name

    @TypeConverter fun stringToMatchStatus(v: String): MatchStatus = MatchStatus.valueOf(v)

    @TypeConverter fun throwRingToString(v: ThrowRing): String = v.name

    @TypeConverter fun stringToThrowRing(v: String): ThrowRing = ThrowRing.valueOf(v)

    @TypeConverter fun turnOutcomeToString(v: TurnOutcome): String = v.name

    @TypeConverter fun stringToTurnOutcome(v: String): TurnOutcome = TurnOutcome.valueOf(v)

    @TypeConverter fun captureModeToString(v: CaptureMode): String = v.name

    @TypeConverter fun stringToCaptureMode(v: String): CaptureMode = CaptureMode.valueOf(v)

    @TypeConverter
    fun marksToString(marks: Map<CricketNumber, Int>): String = marks.entries.joinToString(";") { "${it.key.name}:${it.value}" }

    @TypeConverter
    fun stringToMarks(raw: String): Map<CricketNumber, Int> =
        if (raw.isBlank()) {
            emptyMap()
        } else {
            raw.split(";").associate { entry ->
                val (name, count) = entry.split(":")
                CricketNumber.valueOf(name) to count.toInt()
            }
        }
}
