package com.fishhawk.driftinglibraryandroid.repository.remote.service

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.repository.remote.model.ProviderInfo
import retrofit2.http.QueryMap

interface RemoteProviderService {
    @GET("/provider/list")
    suspend fun getProviders(): List<ProviderInfo>

    @GET("/provider/item/{providerId}/detail")
    suspend fun getProviderDetail(
        @Path("providerId") providerId: String
    ): ProviderDetail

    @GET("/provider/item/{providerId}/search")
    suspend fun search(
        @Path("providerId") providerId: String,
        @Query("keywords") keywords: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/provider/item/{providerId}/popular")
    suspend fun getPopular(
        @Path("providerId") providerId: String,
        @Query("page") page: Int,
        @QueryMap option:Map<String, Int>
    ): List<MangaOutline>

    @GET("/provider/item/{providerId}/latest")
    suspend fun getLatest(
        @Path("providerId") providerId: String,
        @Query("page") page: Int,
        @QueryMap option:Map<String, Int>
    ): List<MangaOutline>

    @GET("/provider/item/{providerId}/category")
    suspend fun getCategory(
        @Path("providerId") providerId: String,
        @Query("page") page: Int,
        @QueryMap option:Map<String, Int>
    ): List<MangaOutline>

    @GET("/provider/item/{providerId}/manga/{mangaId}")
    suspend fun getManga(
        @Path("providerId") providerId: String,
        @Path("mangaId") id: String
    ): MangaDetail

    @GET("/provider/item/{providerId}/chapter/{mangaId}/{chapterId}")
    suspend fun getChapterContent(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Path("chapterId") chapterId: String
    ): List<String>
}