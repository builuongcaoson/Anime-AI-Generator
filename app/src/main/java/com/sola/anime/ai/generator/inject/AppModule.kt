package com.sola.anime.ai.generator.inject

import android.content.Context
import androidx.room.Room
import com.basic.data.PreferencesConfig
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.Database
import com.sola.anime.ai.generator.data.db.query.IapPreviewDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideContext(): Context = App.app

    @Provides
    @Singleton
    fun providePreferences(context: Context): Preferences {
        val sharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val rxSharedPreferences = RxSharedPreferences.create(sharedPreferences)
        return Preferences(context, rxSharedPreferences)
    }

    @Provides
    @Singleton
    fun providePreferencesConfig(context: Context): PreferencesConfig {
        val sharedPreferences = context.getSharedPreferences("PreferencesConfig", Context.MODE_PRIVATE)
        val rxSharedPreferences = RxSharedPreferences.create(sharedPreferences)
        return PreferencesConfig(context, rxSharedPreferences)
    }

    // Database

    @Provides
    @Singleton
    fun provideDatabase(context: Context): Database = Room.databaseBuilder(
        context,
        Database::class.java,
        Database.DB_NAME
    ).fallbackToDestructiveMigration().allowMainThreadQueries().build()

    @Provides
    @Singleton
    fun provideStyleDao(database: Database): StyleDao = database.styleDao()

    @Provides
    @Singleton
    fun provideIapPreviewDao(database: Database): IapPreviewDao = database.iapPreviewDao()

}