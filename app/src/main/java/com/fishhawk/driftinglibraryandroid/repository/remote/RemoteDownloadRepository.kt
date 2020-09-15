package com.fishhawk.driftinglibraryandroid.repository.remote

import retrofit2.Retrofit
import com.fishhawk.driftinglibraryandroid.repository.Result
import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadTask
import com.fishhawk.driftinglibraryandroid.repository.remote.service.RemoteDownloadService

class RemoteDownloadRepository : BaseRemoteRepository<RemoteDownloadService>() {
    fun connect(url: String?, builder: Retrofit?) {
        this.url = url
        this.service = builder?.create(RemoteDownloadService::class.java)
    }

    suspend fun getAllDownloadTasks(): Result<List<DownloadTask>> =
        resultWrap { it.getAllDownloadTasks() }

    suspend fun startAllDownloadTasks(): Result<List<DownloadTask>> =
        resultWrap { it.startAllDownloadTasks() }

    suspend fun pauseAllDownloadTasks(): Result<List<DownloadTask>> =
        resultWrap { it.pauseAllDownloadTasks() }

    suspend fun postDownloadTask(
        providerId: String,
        sourceManga: String,
        targetManga: String
    ): Result<DownloadTask> =
        resultWrap { it.postDownloadTask(providerId, sourceManga, targetManga) }

    suspend fun deleteDownloadTask(id: String): Result<DownloadTask> =
        resultWrap { it.deleteDownloadTask(id) }

    suspend fun startDownloadTask(id: String): Result<DownloadTask> =
        resultWrap { it.startDownloadTask(id) }

    suspend fun pauseDownloadTask(id: String): Result<DownloadTask> =
        resultWrap { it.pauseDownloadTask(id) }
}