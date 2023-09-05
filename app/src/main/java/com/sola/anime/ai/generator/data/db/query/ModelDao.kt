package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.model.Model
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {

    // Query

    @Query("SELECT * FROM Models")
    fun getAll(): List<Model>

    @Query("SELECT * FROM Models WHERE isDislike = :isDislike")
    fun getAllDislikeLive(isDislike: Boolean = false): LiveData<List<Model>>

    @Query("SELECT * FROM Models")
    fun getAllLive(): LiveData<List<Model>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: Model): List<Long>

    @Query("DELETE FROM Models")
    fun deleteAll()

    // Update
    @Update
    fun updates(vararg objects: Model)

    // Find

    @Query("SELECT * FROM Models WHERE id =:id LIMIT 1")
    fun findById(id: Long): Model?

    @Query("SELECT * FROM Models WHERE id =:id LIMIT 1")
    fun findByIdLive(id: Long): LiveData<Model?>

}