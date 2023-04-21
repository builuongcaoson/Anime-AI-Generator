package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.artprocess.ArtProcess

@Dao
interface ArtProcessDao {

    // Query

    @Query("SELECT * FROM ArtProcesses")
    fun getAll(): List<ArtProcess>

    @Query("SELECT * FROM ArtProcesses")
    fun getAllLive(): LiveData<List<ArtProcess>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: ArtProcess): List<Long>

    @Query("DELETE FROM ArtProcesses")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM ArtProcesses WHERE id =:id LIMIT 1")
    fun findById(id: Int): ArtProcess?

}