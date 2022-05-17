package com.fishhawk.lisu.data.remote.service

import com.fishhawk.lisu.data.remote.model.MangaDetailDto
import com.fishhawk.lisu.data.remote.model.MangaDto
import com.fishhawk.lisu.data.remote.model.ProviderDto
import retrofit2.http.*

interface RemoteProviderService {
    @GET("/provider")
    suspend fun listProvider(): List<ProviderDto>

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

    @GET("/provider/{providerId}/manga/{mangaId}/content/{collectionId}/{chapterId}")
    suspend fun getContent(
        @Path("providerId") providerId: String,
        @Path("mangaId") mangaId: String,
        @Path("collectionId") collectionId: String,
        @Path("chapterId") chapterId: String
    ): List<String>
}