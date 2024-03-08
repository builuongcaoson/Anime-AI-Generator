package com.sola.anime.ai.generator.inject

import android.content.Context
import androidx.room.Room
import com.basic.data.LsPrefs
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.gson.GsonBuilder
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.Database
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.data.manager.*
import com.sola.anime.ai.generator.data.repo.DetectFaceRepositoryImpl
import com.sola.anime.ai.generator.data.repo.DezgoApiRepositoryImpl
import com.sola.anime.ai.generator.data.repo.FileRepositoryImpl
import com.sola.anime.ai.generator.data.repo.HistoryRepositoryImpl
import com.sola.anime.ai.generator.data.repo.ServerApiRepositoryImpl
import com.sola.anime.ai.generator.data.repo.SyncRepositoryImpl
import com.sola.anime.ai.generator.data.repo.UpscaleApiRepositoryImpl
import com.sola.anime.ai.generator.domain.manager.*
import com.sola.anime.ai.generator.domain.repo.DetectFaceRepository
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.domain.repo.FileRepository
import com.sola.anime.ai.generator.domain.repo.HistoryRepository
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import com.sola.anime.ai.generator.domain.repo.UpscaleApiRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import com.sola.anime.ai.generator.inject.ls.LsApi
import com.sola.anime.ai.generator.inject.upscale.UpscaleApi
import com.sola.anime.ai.generator.inject.server.ServerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideFirebaseAnalytic(): FirebaseAnalytics = Firebase.analytics

    @Provides
    @Singleton
    fun provideDatabaseReference(): DatabaseReference = Firebase.database.reference

    @Provides
    @Singleton
    fun providePreferences(context: Context): Preferences {
        val sharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        val rxSharedPreferences = RxSharedPreferences.create(sharedPreferences)
        return Preferences(rxSharedPreferences)
    }

    @Provides
    @Singleton
    fun providePreferencesConfig(context: Context): LsPrefs {
        val sharedPreferences = context.getSharedPreferences("LsPrefs", Context.MODE_PRIVATE)
        val rxSharedPreferences = RxSharedPreferences.create(sharedPreferences)
        return LsPrefs(context, rxSharedPreferences)
    }

    @Provides
    @Singleton
    fun provideFaceDetectorOptions(): FirebaseVisionFaceDetectorOptions {
        return FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
            .build()
    }

    @Provides
    @Singleton
    fun provideFaceDetector(
        options: FirebaseVisionFaceDetectorOptions = provideFaceDetectorOptions()
    ): FirebaseVisionFaceDetector {
        return FirebaseVision.getInstance().getVisionFaceDetector(options)
    }

    // Server

    @Provides
    @Singleton
    fun provideLsApi(): LsApi {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d(message)
        }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val networkInterceptor = Interceptor {
            val request = it.request().newBuilder().build()
            it.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain
                    .request()
                    .newBuilder()

                chain.proceed(requestBuilder.build())
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

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constraint.Ls.URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(LsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDezgoApi(): DezgoApi {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d(message)
        }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val networkInterceptor = Interceptor {
            val request = it.request().newBuilder().build()
            it.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain
                    .request()
                    .newBuilder()

                chain.proceed(requestBuilder.build())
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

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constraint.Dezgo.URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(DezgoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideServerApi(): ServerApi {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d(message)
        }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val networkInterceptor = Interceptor {
            val request = it.request().newBuilder().build()
            it.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain
                    .request()
                    .newBuilder()

                chain.proceed(requestBuilder.build())
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

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constraint.Server.URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ServerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUpscaleApi(configApp: ConfigApp): UpscaleApi {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d(message)
        }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val networkInterceptor = Interceptor {
            val request = it.request().newBuilder().build()
            it.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain
                    .request()
                    .newBuilder()

                requestBuilder.addHeader(Constraint.Upscale.HEADER_RAPID_KEY, configApp.keyUpscale)
                requestBuilder.addHeader(Constraint.Upscale.HEADER_RAPID_HOST, Constraint.Upscale.RAPID_HOST)

                chain.proceed(requestBuilder.build())
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

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constraint.Upscale.URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(UpscaleApi::class.java)
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
    fun provideModelDao(database: Database): ModelDao = database.modelDao()

    @Provides
    @Singleton
    fun provideIapDao(database: Database): IAPDao = database.iapDao()

    @Provides
    @Singleton
    fun provideExploreDao(database: Database): ExploreDao = database.exploreDao()

    @Provides
    @Singleton
    fun provideProcessDao(database: Database): ProcessDao = database.processDao()

    @Provides
    @Singleton
    fun provideHistoryDao(database: Database): HistoryDao = database.historyDao()

    @Provides
    @Singleton
    fun provideFolderDao(database: Database): FolderDao = database.folderDao()

    @Provides
    @Singleton
    fun providePhotoStorageDao(database: Database): PhotoStorageDao = database.photoStorageDao()

    @Provides
    @Singleton
    fun provideLoRAGroupDao(database: Database): LoRAGroupDao = database.loRAGroupDao()

    // Repository

    @Provides
    @Singleton
    fun provideDezgoApiRepositoryImpl(repo: DezgoApiRepositoryImpl): DezgoApiRepository = repo

    @Provides
    @Singleton
    fun provideServerApiRepositoryImpl(repo: ServerApiRepositoryImpl): ServerApiRepository = repo

    @Provides
    @Singleton
    fun provideUpscaleApiRepositoryImpl(repo: UpscaleApiRepositoryImpl): UpscaleApiRepository = repo

    @Provides
    @Singleton
    fun provideHistoryRepositoryImpl(repo: HistoryRepositoryImpl): HistoryRepository = repo

    @Provides
    @Singleton
    fun provideFileRepositoryImpl(repo: FileRepositoryImpl): FileRepository = repo

    @Provides
    @Singleton
    fun provideDetectFaceRepositoryImpl(repo: DetectFaceRepositoryImpl): DetectFaceRepository = repo

    @Provides
    @Singleton
    fun provideSyncRepositoryImpl(repo: SyncRepositoryImpl): SyncRepository = repo

    // Manager

    @Provides
    @Singleton
    fun provideAnalyticManagerImpl(manager: AnalyticManagerImpl): AnalyticManager = manager

    @Provides
    @Singleton
    fun provideUserPremiumManagerImpl(manager: UserPremiumManagerImpl): UserPremiumManager = manager

    @Provides
    @Singleton
    fun provideAdmobManagerImpl(manager: AdmobManagerImpl): AdmobManager = manager

    @Provides
    @Singleton
    fun providePermissionManagerImpl(manager: PermissionManagerImpl): PermissionManager = manager

    @Provides
    @Singleton
    fun provideNotificationManagerImpl(manager: NotificationManagerImpl): NotificationManager = manager


}