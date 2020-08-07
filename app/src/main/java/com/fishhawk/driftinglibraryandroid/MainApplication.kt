package com.fishhawk.driftinglibraryandroid

import android.app.Application
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.room.Room
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.ServerInfoRepository
import com.fishhawk.driftinglibraryandroid.repository.data.ServerInfo
import com.fishhawk.driftinglibraryandroid.repository.local.ApplicationDatabase
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryService
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainApplication : Application() {
    private lateinit var database: ApplicationDatabase
    lateinit var readingHistoryRepository: ReadingHistoryRepository
    lateinit var serverInfoRepository: ServerInfoRepository

    private lateinit var selectedServerInfo: LiveData<ServerInfo>
    val remoteLibraryRepository: RemoteLibraryRepository = RemoteLibraryRepository()

    override fun onCreate() {
        super.onCreate()
        SettingsHelper.initialize(this)

        database = Room.databaseBuilder(
            applicationContext,
            ApplicationDatabase::class.java, "Test.db"
        ).build()
        readingHistoryRepository = ReadingHistoryRepository(database.readingHistoryDao())
        serverInfoRepository = ServerInfoRepository(database.serverInfoDao())

        runBlocking {
            val selectedServerInfoValue = serverInfoRepository.selectServerInfo(
                SettingsHelper.selectedServer.getValueDirectly()
            )
            selectServer(selectedServerInfoValue)
        }

        selectedServerInfo = SettingsHelper.selectedServer.switchMap {
            serverInfoRepository.observeServerInfo(it)
        }

        selectedServerInfo.observeForever { serverInfo ->
            selectServer(serverInfo)
        }
    }

    private fun selectServer(serverInfo: ServerInfo?) {
        if (serverInfo == null) {
            remoteLibraryRepository.url = null
            remoteLibraryRepository.service = null
        } else {
            val url = formatAddress(serverInfo.address)
            rebuildRemoteLibraryRepositoryIfNeeded(url)
        }
    }

    private fun formatAddress(url: String): String {
        var newUrl = url
        newUrl = if (URLUtil.isNetworkUrl(newUrl)) newUrl else "http://${newUrl}"
        newUrl = if (newUrl.last() == '/') newUrl else "$newUrl/"
        return newUrl
    }

    private fun rebuildRemoteLibraryRepositoryIfNeeded(url: String) {
        if (remoteLibraryRepository.url != url) {
            remoteLibraryRepository.url = url
            remoteLibraryRepository.service =
                try {
                    Retrofit.Builder()
                        .baseUrl(url)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                } catch (e: Throwable) {
                    null
                }?.create(RemoteLibraryService::class.java)
        }
    }
}