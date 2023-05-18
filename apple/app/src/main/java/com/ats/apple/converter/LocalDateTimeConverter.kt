//package com.ats.apple.converter
//
//import android.annotation.SuppressLint
//import com.google.gson.*
//import java.lang.reflect.Type
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//
//class LocalDateTimeConverter : JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
//    @SuppressLint("NewApi")
//    override fun deserialize(
//        json: JsonElement,
//        typeOfT: Type,
//        context: JsonDeserializationContext
//    ): LocalDateTime {
//        return LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_DATE_TIME)
//    }
//
//    @SuppressLint("NewApi")
//    override fun serialize(
//        src: LocalDateTime,
//        typeOfSrc: Type,
//        context: JsonSerializationContext
//    ): JsonElement {
//        val dateTimeString = src.format(DateTimeFormatter.ISO_DATE_TIME)
//        return JsonPrimitive(dateTimeString)
//    }
//}
