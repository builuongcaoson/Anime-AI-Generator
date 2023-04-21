package com.sola.anime.ai.generator.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sola.anime.ai.generator.data.db.query.ArtProcessDao
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.IapPreviewDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.domain.model.config.artprocess.ArtProcess
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.iap.IapPreview
import com.sola.anime.ai.generator.domain.model.config.style.Style

@Database(entities = [Style::class, IapPreview::class, Explore::class, ArtProcess::class], version = 1)
abstract class Database : RoomDatabase() {

    abstract fun styleDao(): StyleDao

    abstract fun iapPreviewDao(): IapPreviewDao

    abstract fun exploreDao(): ExploreDao

    abstract fun artProcessDao(): ArtProcessDao

    companion object {
        const val DB_NAME = "App_database"
    }

}