package com.fishhawk.driftinglibraryandroid

import android.app.Application
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.fishhawk.driftinglibraryandroid.repository.local.model.ServerInfo
import com.fishhawk.driftinglibraryandroid.repository.local.ApplicationDatabase
import com.fishhawk.driftinglibraryandroid.repository.local.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteDownloadRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteProviderRepository
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteSubscriptionRepository
import com.fishhawk.driftinglibraryandroid.preference.GlobalPreference

class MainApplication : Application() {
    private lateinit var database: ApplicationDatabase
    lateinit var selectedServerInfo: LiveData<ServerInfo>
    private var selectedUrl: String? = null

    lateinit var readingHistoryRepository: ReadingHistoryRepository
    lateinit var serverInfoRepository: ServerInfoRepository

    var url: String? = null
    val remoteLibraryRepository = RemoteLibraryRepository()
    val remoteProviderRepository = RemoteProviderRepository()
    val remoteDownloadRepository = RemoteDownloadRepository()
    val remoteSubscriptionRepository = RemoteSubscriptionRepository()

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
                GlobalPreference.selectedServer.getValueDirectly()
            )
            selectServer(selectedServerInfoValue)
        }

        selectedServerInfo = GlobalPreference.selectedServer.switchMap {
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
        remoteDownloadRepository.connect(url, retrofit)
        remoteSubscriptionRepository.connect(url, retrofit)
    }
}