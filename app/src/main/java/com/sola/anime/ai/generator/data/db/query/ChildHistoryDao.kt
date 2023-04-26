package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.history.ChildHistory

@Dao
interface ChildHistoryDao {

    // Query

    @Query("SELECT * FROM ChildHistories")
    fun getAll(): List<ChildHistory>

    @Query("SELECT * FROM ChildHistories ORDER BY createAt DESC")
    fun getAllLive(): LiveData<List<ChildHistory>>

    @Query("SELECT * FROM ChildHistories WHERE historyId =:historyId ORDER BY createAt DESC")
    fun getAllWithHistoryIdLive(historyId: Long): LiveData<List<ChildHistory>>

    // Inserts or deletes

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserts(vararg objects: ChildHistory): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(objects: ChildHistory): Long

    @Query("DELETE FROM ChildHistories")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM ChildHistories WHERE id =:id LIMIT 1")
    fun findById(id: Long): ChildHistory?

}