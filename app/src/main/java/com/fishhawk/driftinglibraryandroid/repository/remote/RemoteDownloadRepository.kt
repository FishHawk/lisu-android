package com.fishhawk.driftinglibraryandroid.repository.remote

import retrofit2.Retrofit
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadDesc
import com.fishhawk.driftinglibraryandroid.repository.remote.service.RemoteDownloadService

class RemoteDownloadRepository : BaseRemoteRepository<RemoteDownloadService>() {
    fun connect(url: String?, builder: Retrofit?) {
        this.url = url
        this.service = builder?.create(RemoteDownloadService::class.java)
    }

    suspend fun getAllDownloadTasks(): Result<List<DownloadDesc>> =
        resultWrap { it.getAllDownloadTasks() }

    suspend fun startAllDownloadTasks(): Result<List<DownloadDesc>> =
        resultWrap { it.startAllDownloadTasks() }

    suspend fun pauseAllDownloadTasks(): Result<List<DownloadDesc>> =
        resultWrap { it.pauseAllDownloadTasks() }

    suspend fun postDownloadTask(
        providerId: String,
        sourceManga: String,
        targetManga: String
    ): Result<DownloadDesc> =
        resultWrap { it.postDownloadTask(providerId, sourceManga, targetManga) }

    suspend fun deleteDownloadTask(id: String): Result<DownloadDesc> =
        resultWrap { it.deleteDownloadTask(id) }

    suspend fun startDownloadTask(id: String): Result<DownloadDesc> =
        resultWrap { it.startDownloadTask(id) }

    suspend fun pauseDownloadTask(id: String): Result<DownloadDesc> =
        resultWrap { it.pauseDownloadTask(id) }
}