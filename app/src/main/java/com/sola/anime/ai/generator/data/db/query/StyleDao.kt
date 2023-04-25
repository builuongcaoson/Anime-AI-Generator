package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.style.Style

@Dao
interface StyleDao {

    // Query

    @Query("SELECT * FROM Styles")
    fun getAll(): List<Style>

    @Query("SELECT * FROM Styles")
    fun getAllLive(): LiveData<List<Style>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: Style): List<Long>

    @Query("DELETE FROM Styles")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM Styles WHERE id =:id LIMIT 1")
    fun findById(id: Long): Style?

}