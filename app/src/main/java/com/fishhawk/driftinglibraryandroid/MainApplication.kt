package com.fishhawk.driftinglibraryandroid

import android.app.Application
import android.webkit.URLUtil
import androidx.room.Room
import com.fishhawk.driftinglibraryandroid.repository.ReadingHistoryRepository
import com.fishhawk.driftinglibraryandroid.repository.RemoteLibraryRepository
import com.fishhawk.driftinglibraryandroid.repository.local.ApplicationDatabase
import com.fishhawk.driftinglibraryandroid.repository.remote.RemoteLibraryService
import com.fishhawk.driftinglibraryandroid.setting.SettingsHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainApplication : Application() {
    private lateinit var database: ApplicationDatabase
    lateinit var readingHistoryRepository: ReadingHistoryRepository
    lateinit var remoteLibraryRepository: RemoteLibraryRepository

    override fun onCreate() {
        super.onCreate()
        SettingsHelper.initialize(this)

        database = Room.databaseBuilder(
            applicationContext,
            ApplicationDatabase::class.java, "Test.db"
        ).build()
        readingHistoryRepository = ReadingHistoryRepository(database.readingHistoryDao())

        var url = SettingsHelper.libraryAddress.getValueDirectly()
        url = if (URLUtil.isNetworkUrl(url)) url else "http://${url}"
        url = if (url.last() == '/') url else "$url/"
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        remoteLibraryRepository = RemoteLibraryRepository(
            url, retrofit.create(RemoteLibraryService::class.java)
        )
    }

    fun setLibraryAddress(inputUrl: String): Boolean {
        var url = inputUrl
        url = if (URLUtil.isNetworkUrl(url)) url else "http://${url}"
        url = if (url.last() == '/') url else "$url/"
        val retrofit = try {
            Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        } catch (e: Throwable) {
            null
        }

        retrofit?.let {
            remoteLibraryRepository.url = url
            remoteLibraryRepository.service = retrofit.create(RemoteLibraryService::class.java)
            GlobalScope.launch {
                readingHistoryRepository.clearReadingHistory()
            }
        }
        return retrofit != null
    }
}