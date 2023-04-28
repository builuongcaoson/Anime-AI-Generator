package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.Folder

@Dao
interface FolderDao {

    // Query

    @Query("SELECT * FROM Folders")
    fun getAll(): List<Folder>

    @Query("SELECT * FROM Folders")
    fun getAllLive(): LiveData<List<Folder>>

    @Query("SELECT * FROM Folders WHERE id =:id LIMIT 1")
    fun getWithIdLive(id: Long): LiveData<Folder?>

    // Inserts or deletes

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserts(vararg objects: Folder): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(objects: Folder): Long

    @Query("DELETE FROM Folders")
    fun deleteAll()

    // Update

    @Update
    fun update(vararg objects: Folder)

    // Find

    @Query("SELECT * FROM Folders WHERE id = :id LIMIT 1")
    fun findById(id: Long): Folder?

}