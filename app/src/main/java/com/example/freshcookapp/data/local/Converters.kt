package com.example.freshcookapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    // Chuyển List<String> thành chuỗi JSON để lưu vào Room
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Gson().toJson(value ?: emptyList<String>())
    }

    // Chuyển chuỗi JSON từ Room ngược lại thành List<String>
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}