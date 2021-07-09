package com.fishhawk.driftinglibraryandroid

import android.app.Application
import android.content.Context
import android.net.Network
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.fishhawk.driftinglibraryandroid.data.database.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.data.database.ApplicationDatabase
import com.fishhawk.driftinglibraryandroid.data.database.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.data.database.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.data.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.data.preference.GlobalPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class MainApplication : Application() {
    private lateinit var database: ApplicationDatabase
    private lateinit var selectedServerInfo: LiveData<ServerInfo>
    private var selectedUrl: String? = null

    lateinit var readingHistoryRepository: ReadingHistoryRepository
    lateinit var serverInfoRepository: ServerInfoRepository

    val remoteLibraryRepository = RemoteLibraryRepository()
    val remoteProviderRepository = RemoteProviderRepository()

    override fun onCreate() {
        super.onCreate()
        GlobalPreference.initialize(this)

        database = Room.databaseBuilder(
            applicationContext,
            ApplicationDatabase::class.java,
            "Test.db"
        ).build()

        readingHistoryRepository = ReadingHistoryRepository(database.readingHistoryDao())
        serverInfoRepository = ServerInfoRepository(database.serverInfoDao())

        runBlocking {
            val selectedServerInfoValue = serverInfoRepository.selectServerInfo(
                GlobalPreference.selectedServer.get()
            )
            selectServer(selectedServerInfoValue)
        }

        selectedServerInfo = GlobalPreference.selectedServer.asFlow().asLiveData().switchMap {
            serverInfoRepository.observeServerInfo(it)
        }

        selectedServerInfo.observeForever { serverInfo ->
            selectServer(serverInfo)
        }
    }

    private fun selectServer(serverInfo: ServerInfo?) {
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
        NetworkModule.remoteLibraryRepository.connect(url, retrofit)
        NetworkModule.remoteProviderRepository.connect(url, retrofit)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    val remoteLibraryRepository = RemoteLibraryRepository()
    val remoteProviderRepository = RemoteProviderRepository()

    @Provides
    @Singleton
    fun provideLibraryRepository() = remoteLibraryRepository

    @Provides
    @Singleton
    fun provideProviderRepository() = remoteProviderRepository

//    @Singleton
//    @Provides
//    fun database(@ApplicationContext applicationContext: Context) =
//        Room.databaseBuilder(
//            applicationContext,
//            ApplicationDatabase::class.java,
//            "Test.db"
//        ).build()
//
//
//    @Singleton
//    @Provides
//    fun provideReadingHistoryRepository(db: ApplicationDatabase) =
//        ReadingHistoryRepository(db.readingHistoryDao())

    @Singleton
    @Provides
    fun provideReadingHistoryRepository(
        @ApplicationContext applicationContext: Context
    ) = (applicationContext as MainApplication).readingHistoryRepository

    @Singleton
    @Provides
    fun provideServer(
        @ApplicationContext applicationContext: Context
    ) = (applicationContext as MainApplication).serverInfoRepository
}
