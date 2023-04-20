package com.sola.anime.ai.generator.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sola.anime.ai.generator.data.db.query.IapPreviewDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.domain.model.config.style.Style

@Database(entities = [Style::class], version = 1)
abstract class Database : RoomDatabase() {

    abstract fun styleDao(): StyleDao

    abstract fun iapPreviewDao(): IapPreviewDao

    companion object {
        const val DB_NAME = "App_database"
    }

}