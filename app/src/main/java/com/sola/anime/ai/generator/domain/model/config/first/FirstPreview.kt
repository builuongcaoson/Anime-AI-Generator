package com.sola.anime.ai.generator.domain.model.config.first

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class FirstPreview(
    @DrawableRes val preview: Int,
    var ratio: String
)