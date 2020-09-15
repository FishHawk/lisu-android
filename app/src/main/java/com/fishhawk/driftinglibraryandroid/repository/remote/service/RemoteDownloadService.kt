package com.fishhawk.driftinglibraryandroid.repository.remote.service

import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadTask
import retrofit2.http.*

interface RemoteDownloadService {
    @GET("/downloads")
    suspend fun getAllDownloadTasks(): List<DownloadTask>

    @PATCH("/downloads/start")
    suspend fun startAllDownloadTasks(): List<DownloadTask>

    @PATCH("/downloads/pause")
    suspend fun pauseAllDownloadTasks(): List<DownloadTask>

    @FormUrlEncoded
    @POST("/download")
    suspend fun postDownloadTask(
        @Field("providerId") providerId: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): DownloadTask

    @DELETE("/download/{id}")
    suspend fun deleteDownloadTask(@Path("id") id: String): DownloadTask

    @PATCH("/download/{id}/start")
    suspend fun startDownloadTask(@Path("id") id: String): DownloadTask

    @PATCH("/download/{id}/pause")
    suspend fun pauseDownloadTask(@Path("id") id: String): DownloadTask
}