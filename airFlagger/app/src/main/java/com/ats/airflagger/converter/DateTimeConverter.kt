package com.ats.airflagger.converter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDateTime

class DateTimeConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun fromDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}