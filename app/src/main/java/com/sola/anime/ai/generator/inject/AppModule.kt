package com.sola.anime.ai.generator.inject

import android.content.Context
import androidx.room.Room
import com.basic.data.PreferencesConfig
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.Database
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.data.repo.DezgoApiRepositoryImpl
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
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

    // Server
    @Provides
    @Singleton
    fun provideNetworkInterceptor(): Interceptor {
        return Interceptor {
            val request = it.request().newBuilder().build()
            it.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor { message ->
            Timber.d(message)
        }
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        networkInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain
                    .request()
                    .newBuilder()
                    .addHeader("X-RapidAPI-Key", Constraint.Api.DEZGO_API_KEY)
                    .addHeader("X-RapidAPI-Host", Constraint.Api.DEZGO_API_HOST)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(networkInterceptor)
            .addNetworkInterceptor(StethoInterceptor())
            .hostnameVerifier { _, _ -> true }
            .retryOnConnectionFailure(false)
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    @Singleton
    fun provideDezgoApi(
        okHttpClient: OkHttpClient
    ): DezgoApi {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dezgo.p.rapidapi.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(DezgoApi::class.java)
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

    @Provides
    @Singleton
    fun provideExploreDao(database: Database): ExploreDao = database.exploreDao()

    @Provides
    @Singleton
    fun provideArtProcessDao(database: Database): ArtProcessDao = database.artProcessDao()

    @Provides
    @Singleton
    fun provideHistoryDao(database: Database): HistoryDao = database.historyDao()

    // Repository
    @Provides
    @Singleton
    fun provideDezgoApiRepositoryImpl(repo: DezgoApiRepositoryImpl): DezgoApiRepository = repo

}