package com.lym.quietmind.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converters for Room to store custom object types.
 */
class Converters {
    private val gson = Gson()

    // --- List<Double> (For legacy support, might not be heavily used if we moved to List<Interruption>) ---
    @TypeConverter
    fun fromDoubleList(list: List<Double>?): String {
        if (list == null) return "[]"
        return gson.toJson(list)
    }

    @TypeConverter
    fun toDoubleList(data: String?): List<Double> {
        if (data == null) return emptyList()
        val listType = object : TypeToken<List<Double>>() {}.type
        return gson.fromJson(data, listType) ?: emptyList()
    }

    // --- List<String> (For interruption reasons) ---
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data == null) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType) ?: emptyList()
    }
}
