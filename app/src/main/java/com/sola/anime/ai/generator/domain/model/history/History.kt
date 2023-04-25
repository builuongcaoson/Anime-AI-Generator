package com.sola.anime.ai.generator.domain.model.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Histories")
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val prompt: String,
    val pathDir: String?,
    val pathPreview: String,
    val childCount: Int = 1
)