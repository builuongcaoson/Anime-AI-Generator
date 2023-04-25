package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.history.History

@Dao
interface HistoryDao {

    // Query

    @Query("SELECT * FROM Histories")
    fun getAll(): List<History>

    @Query("SELECT * FROM Histories")
    fun getAllLive(): LiveData<List<History>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: History): List<Long>

    @Query("DELETE FROM Histories")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM Histories WHERE id =:id LIMIT 1")
    fun findById(id: Long): History?

}