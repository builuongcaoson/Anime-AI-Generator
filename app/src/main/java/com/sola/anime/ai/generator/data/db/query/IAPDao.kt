package com.sola.anime.ai.generator.data.db.query

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sola.anime.ai.generator.domain.model.config.iap.IAP

@Dao
interface IAPDao {

    // Query

    @Query("SELECT * FROM IAPS")
    fun getAll(): List<IAP>

    @Query("SELECT * FROM IAPS")
    fun getAllLive(): LiveData<List<IAP>>

    // Inserts or deletes

    @Insert
    fun inserts(vararg objects: IAP): List<Long>

    @Query("DELETE FROM IAPS")
    fun deleteAll()

    // Find

    @Query("SELECT * FROM IAPS WHERE id =:id LIMIT 1")
    fun findById(id: Long): IAP?

}