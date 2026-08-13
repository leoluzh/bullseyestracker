package com.bullseyestracker.match.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MatchEntity::class,
        PlayerEntity::class,
        TurnEntity::class,
        ThrowEntity::class,
        DetectionFrameEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BullseyesDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
}
