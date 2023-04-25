package com.sola.anime.ai.generator.domain.model.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ChildHistories")
data class ChildHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val historyId: Long = 0,
    val pathPreview: String,
    val prompt: String,
    val negative_prompt: String,
    val guidance: String,
    val upscale: String,
    val sampler: String,
    val steps: String,
    val model: String,
    val width: String,
    val height: String,
    val seed: String?
)