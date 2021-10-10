package com.fishhawk.driftinglibraryandroid.data.remote.service

import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDto
import retrofit2.http.*

interface RemoteLibraryService {
    @POST("/library/subscribe/{providerId}/{mangaId}")
    suspend fun subscribe(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String
    ): String

    @DELETE("/library/subscribe/{providerId}/{mangaId}")
    suspend fun unsubscribe(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String
    ): String

    @GET("/library/search")
    suspend fun search(
        @Query("page") page: Int,
        @Query("keywords") keywords: String
    ): List<MangaDto>
}