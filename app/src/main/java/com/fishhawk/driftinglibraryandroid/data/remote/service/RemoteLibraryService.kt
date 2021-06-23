package com.fishhawk.driftinglibraryandroid.data.remote.service

import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataDetail
import okhttp3.RequestBody
import retrofit2.http.*

interface RemoteLibraryService {
    @GET("/library/mangas")
    suspend fun listManga(
        @Query("lastTime") lastTime: Long,
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int
    ): List<MangaOutline>

    data class CreateMangaBody(
        val mangaId: String,
        val providerId: String,
        val sourceMangaId: String,
        val keepAfterCompleted: Boolean
    )

    @POST("/library/mangas")
    suspend fun createManga(@Body body: CreateMangaBody)

    @GET("/library/mangas/{mangaId}")
    suspend fun getManga(@Path("mangaId") mangaId: String): MangaDetail

    @DELETE("/library/mangas/{mangaId}")
    suspend fun deleteManga(@Path("mangaId") mangaId: String): String

    @PUT("/library/mangas/{mangaId}/metadata")
    suspend fun updateMangaMetadata(
        @Path("mangaId") mangaId: String,
        @Body metadata: MetadataDetail
    ): MangaDetail

    data class CreateMangaSourceBody(
        val providerId: String,
        val sourceMangaId: String,
        val keepAfterCompleted: Boolean
    )

    @POST("/library/mangas/{mangaId}/source")
    suspend fun createMangaSource(
        @Path("mangaId") mangaId: String,
        @Body body: CreateMangaSourceBody
    )

    @DELETE("/library/mangas/{mangaId}/source")
    suspend fun deleteMangaSource(@Path("mangaId") mangaId: String)

    @POST("/library/mangas/{mangaId}/source/sync")
    suspend fun syncMangaSource(@Path("mangaId") mangaId: String)

    @Multipart
    @PUT("/library/mangas/{mangaId}/cover")
    suspend fun updateMangaThumb(
        @Path("mangaId") mangaId: String,
        @Part("cover\"; filename=\"cover\" ") cover: RequestBody
    ): MangaDetail

    @GET("/library/mangas/{mangaId}/chapters/{collectionId}/{chapterId}")
    suspend fun getChapter(
        @Path("mangaId") mangaId: String,
        @Path("collectionId") collectionId: String,
        @Path("chapterId") chapterId: String
    ): List<String>

    @GET("/library/mangas/{mangaId}/chapters/{chapterId}")
    suspend fun getChapter(
        @Path("mangaId") mangaId: String,
        @Path("chapterId") chapterId: String
    ): List<String>

    @GET("/library/mangas/{mangaId}/chapters/")
    suspend fun getChapter(
        @Path("mangaId") mangaId: String
    ): List<String>
}