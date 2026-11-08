package com.example.wichtly.data.local
import androidx.room.TypeConverter
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
}
