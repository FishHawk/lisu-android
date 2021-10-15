package com.fishhawk.lisu.data.remote.service

import com.fishhawk.lisu.data.remote.model.MangaDto
import retrofit2.http.*

interface RemoteLibraryService {
    @GET("/library/search")
    suspend fun search(
        @Query("page") page: Int,
        @Query("keywords") keywords: String
    ): List<MangaDto>

    @POST("/library/manga/{providerId}/{mangaId}")
    suspend fun createManga(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String
    ): String

    @DELETE("/library/manga/{providerId}/{mangaId}")
    suspend fun deleteManga(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String
    ): String
}