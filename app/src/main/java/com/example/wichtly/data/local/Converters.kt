package com.example.wichtly.data.local
import androidx.room.TypeConverter
import com.example.wichtly.domain.model.DrawType
import java.time.Instant
import java.time.LocalDate

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
