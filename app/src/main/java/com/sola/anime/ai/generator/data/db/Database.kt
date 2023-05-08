package com.sola.anime.ai.generator.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sola.anime.ai.generator.data.db.converter.Converters
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.domain.model.Folder
import com.sola.anime.ai.generator.domain.model.config.artprocess.ArtProcess
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.iap.IapPreview
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.history.History

@Database(
    entities = [
        Style::class,
        IapPreview::class,
        Explore::class,
        ArtProcess::class,
        History::class,
        Folder::class
               ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun styleDao(): StyleDao

    abstract fun iapPreviewDao(): IapPreviewDao

    abstract fun exploreDao(): ExploreDao

    abstract fun artProcessDao(): ArtProcessDao

    abstract fun historyDao(): HistoryDao

    abstract fun folderDao(): FolderDao

    companion object {
        const val DB_NAME = "App_database"
    }

}