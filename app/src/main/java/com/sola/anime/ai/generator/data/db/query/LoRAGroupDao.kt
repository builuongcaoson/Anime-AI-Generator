package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup

@Dao
interface LoRAGroupDao {

    // Query

    @Query("SELECT * FROM LoRAGroups")
    fun getAll(): List<LoRAGroup>

    @Query("SELECT * FROM LoRAGroups")
    fun getAllLive(): LiveData<List<LoRAGroup>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: LoRAGroup): List<Long>

    @Query("DELETE FROM LoRAGroups")
    fun deleteAll()

    // Update

    @Update
    fun updates(vararg objects: LoRAGroup)

    // Find

    @Query("SELECT * FROM LoRAGroups WHERE id =:id LIMIT 1")
    fun findById(id: Long): LoRAGroup?

    @Query("SELECT * FROM LoRAGroups WHERE id =:id LIMIT 1")
    fun findByIdLive(id: Long): LiveData<LoRAGroup?>

}