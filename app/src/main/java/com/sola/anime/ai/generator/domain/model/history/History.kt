package com.sola.anime.ai.generator.domain.model.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Histories")
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "No Style",
    val prompt: String,
    val model: String,
    val childs: ArrayList<ChildHistory> = arrayListOf()
){

    var folderId: Long = 0
    var styleId: Long = -1L
    var isShowPromptHistory: Boolean = true

    var createAt: Long = System.currentTimeMillis()
    var updateAt: Long = System.currentTimeMillis()

}