package com.example.thehive.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromAmenities(list: List<String>?): String =
        list.orEmpty().joinToString(SEP) { it.trim() }

    @TypeConverter
    fun toAmenities(value: String?): List<String> =
        value.orEmpty().split(SEP).map { it.trim() }.filter { it.isNotEmpty() }

    private companion object { const val SEP = "," }
}
