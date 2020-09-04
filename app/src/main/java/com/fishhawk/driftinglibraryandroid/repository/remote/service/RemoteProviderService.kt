package com.fishhawk.driftinglibraryandroid.repository.remote.service

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo

interface RemoteProviderService {
    @GET("/providers")
    suspend fun getProviders(): List<ProviderInfo>

    @GET("/provider/{providerId}/search")
    suspend fun search(
        @Path("providerId") providerId: String,
        @Query("keywords") keywords: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/provider/{providerId}/popular")
    suspend fun getPopular(
        @Path("providerId") providerId: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/provider/{providerId}/latest")
    suspend fun getLatest(
        @Path("providerId") providerId: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/provider/{providerId}/manga/{mangaId}")
    suspend fun getManga(
        @Path("providerId") providerId: String,
        @Path("mangaId") id: String
    ): MangaDetail

    @GET("/provider/{providerId}/chapter/{mangaId}/{chapterId}")
    suspend fun getChapterContent(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Path("chapterId") chapterId: String
    ): List<String>
}