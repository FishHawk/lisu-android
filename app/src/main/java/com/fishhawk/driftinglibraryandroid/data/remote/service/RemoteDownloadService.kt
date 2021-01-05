package com.fishhawk.driftinglibraryandroid.data.remote.service

import com.fishhawk.driftinglibraryandroid.data.remote.model.DownloadDesc
import retrofit2.http.*

interface RemoteDownloadService {
    @GET("/download/list")
    suspend fun getAllDownloadTasks(): List<DownloadDesc>

    @PATCH("/download/list/start")
    suspend fun startAllDownloadTasks(): List<DownloadDesc>

    @PATCH("/download/list/pause")
    suspend fun pauseAllDownloadTasks(): List<DownloadDesc>

    @FormUrlEncoded
    @POST("/download/item")
    suspend fun postDownloadTask(
        @Field("providerId") providerId: String,
        @Field("sourceManga") sourceManga: String,
        @Field("targetManga") targetManga: String
    ): DownloadDesc

    @DELETE("/download/item/{id}")
    suspend fun deleteDownloadTask(@Path("id") id: String): DownloadDesc

    @PATCH("/download/item/{id}/start")
    suspend fun startDownloadTask(@Path("id") id: String): DownloadDesc

    @PATCH("/download/item/{id}/pause")
    suspend fun pauseDownloadTask(@Path("id") id: String): DownloadDesc
}