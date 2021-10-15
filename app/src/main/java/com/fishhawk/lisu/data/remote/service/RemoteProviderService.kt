package com.fishhawk.lisu.data.remote.service

import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.MetadataDto
import com.fishhawk.lisu.data.remote.model.Provider
import okhttp3.RequestBody
import retrofit2.http.*

interface RemoteProviderService {
    @GET("/provider")
    suspend fun listProvider(): List<Provider>

    @GET("/provider/{providerId}/search")
    suspend fun search(
        @Path("providerId") providerId: String,
        @Query("page") page: Int,
        @Query("keywords") keywords: String
    ): List<MangaDto>

    @GET("/provider/{providerId}/board/{boardId}")
    suspend fun getBoard(
        @Path("providerId") providerId: String,
        @Path("boardId") boardId: String,
        @Query("page") page: Int,
        @QueryMap filters: Map<String, Int>
    ): List<MangaDto>

    @GET("/provider/{providerId}/manga/{mangaId}")
    suspend fun getManga(
        @Path("providerId") providerId: String,
        @Path("mangaId") id: String
    ): MangaDetailDto

    @PUT("/provider/{providerId}/mangas/{mangaId}/metadata")
    suspend fun updateMangaMetadata(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Body metadata: MetadataDto
    ): String

    @Multipart
    @PUT("/provider/{providerId}/manga/{mangaId}/cover")
    suspend fun updateMangaCover(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Part("cover\"; filename=\"cover\" ") cover: RequestBody
    ): String

    @GET("/provider/{providerId}/manga/{mangaId}/content/{collectionId}/{chapterId}")
    suspend fun getContent(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Path("collectionId") collectionId: String,
        @Path("chapterId") chapterId: String
    ): List<String>
}