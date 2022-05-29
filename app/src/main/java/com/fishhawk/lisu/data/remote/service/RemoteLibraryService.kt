package com.fishhawk.lisu.data.remote.service

import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MangaKeyDto
import com.fishhawk.lisu.data.remote.model.MangaMetadataDto
import okhttp3.MultipartBody
import retrofit2.http.*

interface RemoteLibraryService {
    @GET("/library/search")
    suspend fun search(
        @Query("page") page: Int,
        @Query("keywords") keywords: String
    ): List<MangaDto>

    @GET("/library/random-manga")
    suspend fun getRandomManga(): MangaDto

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

    @Multipart
    @PUT("/library/manga/{providerId}/{mangaId}/cover")
    suspend fun updateMangaCover(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Part cover: MultipartBody.Part,
    ): String

    @PUT("/library/manga/{providerId}/{mangaId}/metadata")
    suspend fun updateMangaMetadata(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Body metadata: MangaMetadataDto
    ): String

    @POST("/library/manga-delete")
    suspend fun deleteMultipleMangas(
        @Body mangas: List<MangaKeyDto>
    ): String
}