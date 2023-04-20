package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.iap.IapPreview

@Dao
interface IapPreviewDao {

    // Query

    @Query("SELECT * FROM IapPreviews")
    fun getAll(): List<IapPreview>

    @Query("SELECT * FROM IapPreviews")
    fun getAllLive(): LiveData<List<IapPreview>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: IapPreview): List<Long>

    @Query("DELETE FROM IapPreviews")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM IapPreviews WHERE id =:id LIMIT 1")
    fun findById(id: Long): IapPreview?

}