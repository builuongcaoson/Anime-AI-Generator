package com.sola.anime.ai.generator.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.data.db.converter.Converters
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.domain.model.Folder
import com.sola.anime.ai.generator.domain.model.config.process.Process
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.iap.IAP
import com.sola.anime.ai.generator.domain.model.config.style.Style
import com.sola.anime.ai.generator.domain.model.history.History

@Database(
    entities = [
        Style::class,
        IAP::class,
        Explore::class,
        Process::class,
        History::class,
        Folder::class
               ],
    version = Constraint.Info.DATA_VERSION
)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun styleDao(): StyleDao

    abstract fun iapDao(): IAPDao

    abstract fun exploreDao(): ExploreDao

    abstract fun processDao(): ProcessDao

    abstract fun historyDao(): HistoryDao

    abstract fun folderDao(): FolderDao

    companion object {
        const val DB_NAME = "App_database"
    }

}