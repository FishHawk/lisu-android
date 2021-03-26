package com.fishhawk.driftinglibraryandroid.data.remote.service

import com.fishhawk.driftinglibraryandroid.data.remote.model.DownloadDesc
import retrofit2.http.*

interface RemoteDownloadService {
    @GET("/downloads/")
    suspend fun getAllDownloadTasks(): List<DownloadDesc>

    @PATCH("/downloads/start")
    suspend fun startAllDownloadTasks(): List<DownloadDesc>

    @PATCH("/downloads/pause")
    suspend fun pauseAllDownloadTasks(): List<DownloadDesc>

    @FormUrlEncoded
    @POST("/downloads/")
    suspend fun postDownloadTask(
        @Field("providerId") providerId: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): DownloadDesc

    @DELETE("/downloads/{id}")
    suspend fun deleteDownloadTask(@Path("id") id: String): DownloadDesc

    @PATCH("/downloads/{id}/start")
    suspend fun startDownloadTask(@Path("id") id: String): DownloadDesc

    @PATCH("/downloads/{id}/pause")
    suspend fun pauseDownloadTask(@Path("id") id: String): DownloadDesc
}