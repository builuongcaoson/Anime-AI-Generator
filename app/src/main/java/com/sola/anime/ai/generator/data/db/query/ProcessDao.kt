package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.process.Process

@Dao
interface ProcessDao {

    // Query

    @Query("SELECT * FROM Process")
    fun getAll(): List<Process>

    @Query("SELECT * FROM Process")
    fun getAllLive(): LiveData<List<Process>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: Process): List<Long>

    @Query("DELETE FROM Process")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM Process WHERE id =:id LIMIT 1")
    fun findById(id: Long): Process?

}