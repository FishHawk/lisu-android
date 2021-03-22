package com.fishhawk.driftinglibraryandroid.data.remote.service

import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaDetail
import com.fishhawk.driftinglibraryandroid.data.remote.model.MangaOutline
import com.fishhawk.driftinglibraryandroid.data.remote.model.MetadataDetail
import okhttp3.RequestBody
import retrofit2.http.*

interface RemoteLibraryService {
    @GET("library/mangas")
    suspend fun listManga(
        @Query("lastTime") lastTime: Long,
        @Query("keywords") keywords: String,
        @Query("limit") limit: Int
    ): List<MangaOutline>

    @GET("library/mangas/{mangaId}")
    suspend fun getManga(@Path("mangaId") mangaId: String): MangaDetail

    @DELETE("library/mangas/{mangaId}")
    suspend fun deleteManga(@Path("mangaId") mangaId: String): String

    @PUT("library/mangas/{mangaId}/metadata")
    suspend fun updateMangaMetadata(
        @Path("mangaId") mangaId: String,
        @Body metadata: MetadataDetail
    ): MangaDetail

    @Multipart
    @PUT("library/mangas/{mangaId}/thumb")
    suspend fun updateMangaThumb(
        @Path("mangaId") mangaId: String,
        @Part("thumb\"; filename=\"thumb\" ") thumb: RequestBody
    ): MangaDetail

    @GET("library/mangas/{mangaId}/chapters/{collectionId}/{chapterId}")
    suspend fun getChapter(
        @Path("mangaId") mangaId: String,
        @Path("collectionId") collectionId: String,
        @Path("chapterId") chapterId: String
    ): List<String>
}