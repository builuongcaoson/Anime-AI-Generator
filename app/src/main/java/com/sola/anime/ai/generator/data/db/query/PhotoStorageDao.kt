package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.PhotoStorage

@Dao
interface PhotoStorageDao {

    // Query
    @Query("SELECT * FROM PhotoStorages")
    fun getAllLive(): LiveData<List<PhotoStorage>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: PhotoStorage): List<Long>

    @Delete
    fun deletes(vararg objects: PhotoStorage)

    @Query("DELETE FROM PhotoStorages")
    fun deleteAll()

    // Update

    @Update
    fun update(vararg objects: PhotoStorage)

    // Find

    @Query("SELECT * FROM PhotoStorages WHERE id =:id LIMIT 1")
    fun findById(id: Long): PhotoStorage?

    @Query("SELECT * FROM PhotoStorages WHERE id =:id LIMIT 1")
    fun findByIdLive(id: Long): LiveData<PhotoStorage?>

}