package com.sola.anime.ai.generator.data.db.converter

import androidx.room.TypeConverter
import com.basic.common.extension.tryOrNull
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sola.anime.ai.generator.domain.model.history.ChildHistory

class Converters {

    @TypeConverter
    fun childHistoriesToJson(value: ArrayList<ChildHistory>) = tryOrNull { Gson().toJson(value) } ?: ""

    @TypeConverter
    fun jsonToChildHistories(value: String): ArrayList<ChildHistory>{
        return when {
            value.isEmpty() -> arrayListOf()
            else -> tryOrNull { Gson().fromJson(value, object : TypeToken<List<ChildHistory>>() {}.type) } ?: arrayListOf()
        }
    }

}