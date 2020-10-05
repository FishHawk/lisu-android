package com.fishhawk.driftinglibraryandroid.repository.remote.service

import com.fishhawk.driftinglibraryandroid.repository.remote.model.DownloadTask
import retrofit2.http.*

interface RemoteDownloadService {
    @GET("/download/list")
    suspend fun getAllDownloadTasks(): List<DownloadTask>

    @PATCH("/download/list/start")
    suspend fun startAllDownloadTasks(): List<DownloadTask>

    @PATCH("/download/list/pause")
    suspend fun pauseAllDownloadTasks(): List<DownloadTask>

    @FormUrlEncoded
    @POST("/download/item")
    suspend fun postDownloadTask(
        @Field("providerId") providerId: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): DownloadTask

    @DELETE("/download/item/{id}")
    suspend fun deleteDownloadTask(@Path("id") id: String): DownloadTask

    @PATCH("/download/item/{id}/start")
    suspend fun startDownloadTask(@Path("id") id: String): DownloadTask

    @PATCH("/download/item/{id}/pause")
    suspend fun pauseDownloadTask(@Path("id") id: String): DownloadTask
}