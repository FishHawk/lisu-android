package com.fishhawk.driftinglibraryandroid

import android.app.Application
import android.content.Context
import android.webkit.URLUtil
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import com.fishhawk.driftinglibraryandroid.data.database.ApplicationDatabase
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.datastore.PreferenceRepository
import com.fishhawk.driftinglibraryandroid.data.datastore.ProviderBrowseHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.util.interceptor.ProgressInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

lateinit var PR: PreferenceRepository

@HiltAndroidApp
class MainApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var pr: PreferenceRepository

    override fun onCreate() {
        super.onCreate()
        PR = pr
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(applicationContext))
                    .addInterceptor(ProgressInterceptor())
                    .build()
            }
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Singleton
    @Provides
    fun provideRetrofit(preferenceRepository: PreferenceRepository): Flow<Result<Retrofit>?> {
        return preferenceRepository.serverAddress.flow
            .map { address ->
                address
                    .let { if (it.isBlank()) null else it }
                    ?.let { if (URLUtil.isNetworkUrl(it)) it else "http://$it" }
                    ?.let { if (it.last() == '/') it else "$it/" }
            }
            .distinctUntilChanged()
            .map { url ->
                if (url == null)
                    return@map Result.failure(Exception("No server selected"))
                try {
                    Result.success(
                        Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                    )
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }
            .shareIn(
                CoroutineScope(SupervisorJob() + Dispatchers.Main),
                SharingStarted.Eagerly,
                1
            )
    }

    @Provides
    @Singleton
    fun provideLibraryRepository(retrofit: Flow<Result<Retrofit>?>) =
        RemoteLibraryRepository(retrofit)

    @Provides
    @Singleton
    fun provideProviderRepository(retrofit: Flow<Result<Retrofit>?>) =
        RemoteProviderRepository(retrofit)

    @Provides
    @Singleton
    fun database(@ApplicationContext applicationContext: Context) =
        Room.databaseBuilder(
            applicationContext,
            ApplicationDatabase::class.java,
            "Test.db"
        ).build()

    @Provides
    @Singleton
    fun provideReadingHistoryRepository(db: ApplicationDatabase) =
        ReadingHistoryRepository(db.readingHistoryDao())

    @Provides
    @Singleton
    fun preferenceRepository(@ApplicationContext applicationContext: Context) =
        PreferenceRepository(applicationContext)

    @Provides
    @Singleton
    fun provideProviderBrowseHistoryRepository(@ApplicationContext applicationContext: Context) =
        ProviderBrowseHistoryRepository(applicationContext)
}