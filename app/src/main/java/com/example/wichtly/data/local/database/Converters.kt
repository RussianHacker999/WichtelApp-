package com.example.wichtly.data.local.database
import androidx.room.TypeConverter
import com.example.wichtly.domain.model.group.DrawType
import java.time.Instant
import java.time.LocalDate

/**
 * Room type converters for the column types SQLite does not know.
 *
 * - [LocalDate] is stored as ISO text ("2026-12-24"), so it sorts correctly as a string.
 * - [Instant] is stored as epoch milliseconds.
 * - [DrawType] is stored as its enum name; renaming an enum constant therefore needs a migration.
 */
class Converters {
    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromDrawType(drawType: DrawType?): String? {
        return drawType?.name
    }

    @TypeConverter
    fun toDrawType(value: String?): DrawType? {
        return value?.let { DrawType.valueOf(it) }
    }
}
