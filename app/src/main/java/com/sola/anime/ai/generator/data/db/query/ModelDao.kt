package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.model.Model

@Dao
interface ModelDao {

    // Query

    @Query("SELECT * FROM Models")
    fun getAll(): List<Model>

    @Query("SELECT * FROM Models")
    fun getAllLive(): LiveData<List<Model>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: Model): List<Long>

    @Query("DELETE FROM Models")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM Models WHERE id =:id LIMIT 1")
    fun findById(id: Long): Model?

}