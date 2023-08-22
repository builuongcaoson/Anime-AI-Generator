package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.explore.Explore

@Dao
interface ExploreDao {

    // Query

    @Query("SELECT * FROM Explores")
    fun getAll(): List<Explore>

    @Query("SELECT * FROM Explores")
    fun getAllLive(): LiveData<List<Explore>>

    @Query("SELECT * FROM Explores WHERE ratio = :ratio")
    fun getAllRatio2x3Live(ratio: String = "2:3"): LiveData<List<Explore>>

    @Query("SELECT * FROM Explores WHERE ratio != :ratio")
    fun getAllOtherRatio2x3Live(ratio: String = "2:3"): LiveData<List<Explore>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: Explore): List<Long>

    @Query("DELETE FROM Explores")
    fun deleteAll()

    // Update

    @Update
    fun update(vararg objects: Explore)

    // Find

    @Query("SELECT * FROM Explores WHERE id =:id LIMIT 1")
    fun findById(id: Long): Explore?

}