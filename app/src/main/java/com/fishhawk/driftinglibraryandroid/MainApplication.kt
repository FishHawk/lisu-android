package com.fishhawk.driftinglibraryandroid

import android.app.Application
import android.content.Context
import android.webkit.URLUtil
import androidx.room.Room
import com.fishhawk.driftinglibraryandroid.data.database.ApplicationDatabase
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.data.preference.P
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
class MainApplication : Application() {
    @Inject
    lateinit var serverInfoRepository: ServerInfoRepository

    override fun onCreate() {
        super.onCreate()
        P.initialize(this)
        P.selectedServer.asFlow()
            .flatMapLatest { serverInfoRepository.select(it) }
            .onEach { AppModule.selectServer(it) }
            .launchIn(CoroutineScope(SupervisorJob() + Dispatchers.Main))
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private var selectedUrl: String? = null
    private val remoteLibraryRepository = RemoteLibraryRepository()
    private val remoteProviderRepository = RemoteProviderRepository()

    @Provides
    @Singleton
    fun provideLibraryRepository() = remoteLibraryRepository

    @Provides
    @Singleton
    fun provideProviderRepository() = remoteProviderRepository

    @Singleton
    @Provides
    fun database(@ApplicationContext applicationContext: Context) =
        Room.databaseBuilder(
            applicationContext,
            ApplicationDatabase::class.java,
            "Test.db"
        ).build()

    @Singleton
    @Provides
    fun provideReadingHistoryRepository(db: ApplicationDatabase) =
        ReadingHistoryRepository(db.readingHistoryDao())

    @Singleton
    @Provides
    fun provideServerInfoRepository(db: ApplicationDatabase) =
        ServerInfoRepository(db.serverInfoDao())


    fun selectServer(serverInfo: ServerInfo?) {
        val url = serverInfo?.address?.let {
            var newUrl = it
            newUrl = if (URLUtil.isNetworkUrl(newUrl)) newUrl else "http://${newUrl}"
            newUrl = if (newUrl.last() == '/') newUrl else "$newUrl/"
            newUrl
        }

        if (selectedUrl == url) return
        else selectedUrl = url

        val retrofit = url?.let {
            try {
                Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            } catch (e: Throwable) {
                null
            }
        }

        remoteLibraryRepository.connect(url, retrofit)
        remoteProviderRepository.connect(url, retrofit)
    }
}
