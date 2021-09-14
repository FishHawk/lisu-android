package com.fishhawk.driftinglibraryandroid.data.remote.service

import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.ProviderDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.Provider
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface RemoteProviderService {
    @GET("/providers")
    suspend fun listProvider(): List<Provider>

    @GET("/providers/{providerId}")
    suspend fun getProvider(
        @Path("providerId") providerId: String
    ): ProviderDetail

    @GET("/providers/{providerId}/popular")
    suspend fun listPopularManga(
        @Path("providerId") providerId: String,
        @Query("page") page: Int,
        @QueryMap option: Map<String, Int>
    ): List<MangaOutline>

    @GET("/providers/{providerId}/latest")
    suspend fun listLatestManga(
        @Path("providerId") providerId: String,
        @Query("page") page: Int,
        @QueryMap option: Map<String, Int>
    ): List<MangaOutline>

    @GET("/providers/{providerId}/category")
    suspend fun listCategoryManga(
        @Path("providerId") providerId: String,
        @Query("page") page: Int,
        @QueryMap option: Map<String, Int>
    ): List<MangaOutline>

    @GET("/providers/{providerId}/mangas")
    suspend fun listManga(
        @Path("providerId") providerId: String,
        @Query("keywords") keywords: String,
        @Query("page") page: Int
    ): List<MangaOutline>

    @GET("/providers/{providerId}/mangas/{mangaId}")
    suspend fun getManga(
        @Path("providerId") providerId: String,
        @Path("mangaId") id: String
    ): MangaDetail

    @GET("/providers/{providerId}/mangas/{mangaId}/chapters/{chapterId}")
    suspend fun getChapter(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Path("chapterId") chapterId: String
    ): List<String>
}